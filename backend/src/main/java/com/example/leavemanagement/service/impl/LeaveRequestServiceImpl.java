package com.example.leavemanagement.service.impl;

import com.example.leavemanagement.dto.*;
import com.example.leavemanagement.entity.*;
import com.example.leavemanagement.exception.*;
import com.example.leavemanagement.notification.LeaveNotificationService;
import com.example.leavemanagement.repository.*;
import com.example.leavemanagement.security.CurrentUserService;
import com.example.leavemanagement.service.AuditService;
import com.example.leavemanagement.service.LeaveRequestService;
import java.io.IOException;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.web.multipart.MultipartFile;

@Service
@Transactional(readOnly = true)
public class LeaveRequestServiceImpl implements LeaveRequestService {
    private static final List<LeaveRequestStatus> ACTIVE = List.of(LeaveRequestStatus.PENDING, LeaveRequestStatus.APPROVED);
    private static final List<LeaveRequestStatus> APPROVER_STATUSES = List.of(LeaveRequestStatus.PENDING, LeaveRequestStatus.APPROVED, LeaveRequestStatus.REJECTED);
    private final UserRepository users; private final ApproverAssignmentRepository assignments; private final LeaveRequestRepository requests;
    private final AuditService audits; private final CurrentUserService currentUser; private final LeaveNotificationService notifications;
    private final Clock clock; private final long maxAttachmentBytes;

    public LeaveRequestServiceImpl(UserRepository users, ApproverAssignmentRepository assignments, LeaveRequestRepository requests,
                                   AuditService audits, CurrentUserService currentUser, LeaveNotificationService notifications,
                                   @Value("${leave-management.attachment-max-bytes:5242880}") long maxAttachmentBytes) {
        this.users = users; this.assignments = assignments; this.requests = requests; this.audits = audits; this.currentUser = currentUser;
        this.notifications = notifications; this.clock = Clock.systemUTC(); this.maxAttachmentBytes = maxAttachmentBytes;
    }

    @Transactional
    public LeaveRequestResponse create(String username, LeaveRequestCreateRequest input, MultipartFile file) {
        validateInput(input.leaveType(), input.startDate(), input.endDate(), input.reason());
        User employee = employee(username); ApproverAssignment assignment = assignments.findByEmployeeAndActiveTrue(employee).orElseThrow(() -> new ConflictException("No approver is assigned"));
        validateDates(input.startDate(), input.endDate(), employee, null); Attachment attachment = attachment(file);
        Instant now = Instant.now(clock); LeaveRequest request = requests.save(new LeaveRequest(employee, assignment.getApprover(), input.leaveType().trim(), input.startDate(), input.endDate(), input.reason().trim(), input.partialDay(), attachment, now));
        audits.record(request, AuditEventType.CREATED, employee, null, now); afterCommit(notifications::submitted, request); return response(request);
    }

    public LeaveRequestListResponse list(String username, LeaveRequestStatus status, LocalDate startDate, LocalDate endDate) {
        User user = user(username); List<LeaveRequest> result;
        if (user.getRole() == Role.EMPLOYEE) {
            if (status != null || startDate != null || endDate != null) throw new BadRequestException("Filters are available only to approvers");
            result = requests.findByEmployeeOrderBySubmittedAtDesc(user);
        } else if (user.getRole() == Role.APPROVER) {
            if (startDate != null && endDate != null && endDate.isBefore(startDate)) throw new BadRequestException("endDate must not precede startDate");
            result = requests.findByApproverAndStatusInAndOptionalDateRangeOrderBySubmittedAtDesc(user,
                status == null ? APPROVER_STATUSES : List.of(status), startDate, endDate);
        } else throw new ForbiddenException("Role cannot access leave requests");
        return new LeaveRequestListResponse(result.stream().map(this::summary).toList());
    }

    public LeaveRequestResponse detail(String username, UUID id) { return response(visible(username, id)); }

    @Transactional
    public LeaveRequestResponse update(String username, UUID id, LeaveRequestUpdateRequest input, MultipartFile file) {
        validateInput(input.leaveType(), input.startDate(), input.endDate(), input.reason());
        User employee = employee(username); LeaveRequest request = requests.findByIdAndEmployee(id, employee).orElseThrow(() -> new ResourceNotFoundException("Leave request not found"));
        if (request.getStatus() != LeaveRequestStatus.PENDING) throw new ConflictException("Only pending requests can be edited");
        validateDates(input.startDate(), input.endDate(), employee, id); Attachment attachment = file == null || file.isEmpty() ? null : attachment(file); Instant now = Instant.now(clock);
        request.update(input.leaveType().trim(), input.startDate(), input.endDate(), input.reason().trim(), input.partialDay(), attachment, now); audits.record(request, AuditEventType.UPDATED, employee, null, now); afterCommit(notifications::submitted, request); return response(request);
    }

    @Transactional
    public LeaveRequestResponse cancel(String username, UUID id) {
        User employee = employee(username); LeaveRequest request = requests.findByIdAndEmployee(id, employee).orElseThrow(() -> new ResourceNotFoundException("Leave request not found"));
        if (request.getStatus() != LeaveRequestStatus.PENDING && request.getStatus() != LeaveRequestStatus.APPROVED) throw new ConflictException("Request cannot be cancelled");
        Instant now = Instant.now(clock); request.cancel(now); audits.record(request, AuditEventType.CANCELLED, employee, null, now); afterCommit(notifications::cancelled, request); return response(request);
    }

    public AuditHistoryResponse history(String username, UUID id) { return new AuditHistoryResponse(audits.history(visible(username, id)).stream().map(e -> new AuditEventResponse(e.getId(), e.getEventType(), e.getActor().getUsername(), e.getEventTime(), e.getComment())).toList()); }

    @Transactional
    public LeaveRequestResponse approve(String username, UUID id, String comment) { return decide(username, id, LeaveRequestStatus.APPROVED, comment); }
    @Transactional
    public LeaveRequestResponse reject(String username, UUID id, String comment) { if (comment == null || comment.isBlank()) throw new BadRequestException("Rejection comment is required"); return decide(username, id, LeaveRequestStatus.REJECTED, comment); }

    private LeaveRequestResponse decide(String username, UUID id, LeaveRequestStatus target, String comment) {
        User approver = user(username); if (approver.getRole() != Role.APPROVER) throw new ForbiddenException("Approver role required");
        LeaveRequest request = requests.findByIdAndApprover(id, approver).orElseThrow(() -> new ResourceNotFoundException("Leave request not found"));
        if (request.getStatus() != LeaveRequestStatus.PENDING) throw new ConflictException("Request has already been decided");
        Instant now = Instant.now(clock);
        if (requests.decideIfPending(id, approver, target, approver, comment, now, now) != 1) throw new ConflictException("Request has already been decided");
        request = requests.findByIdAndApprover(id, approver).orElseThrow(() -> new ResourceNotFoundException("Leave request not found"));
        audits.record(request, target == LeaveRequestStatus.APPROVED ? AuditEventType.APPROVED : AuditEventType.REJECTED, approver, comment, now);
        afterCommit(target == LeaveRequestStatus.APPROVED ? notifications::approved : notifications::rejected, request); return response(request);
    }

    private User employee(String username) { User user = user(username); if (user.getRole() != Role.EMPLOYEE) throw new ForbiddenException("Employee role required"); return user; }
    private User user(String username) { return users.findByUsername(username).orElseThrow(() -> new ResourceNotFoundException("User not found")); }
    private void validateInput(String leaveType, LocalDate start, LocalDate end, String reason) {
        if (leaveType == null || leaveType.isBlank() || leaveType.length() > 50) throw new BadRequestException("leaveType is required and must be at most 50 characters");
        if (start == null || end == null) throw new BadRequestException("startDate and endDate are required");
        if (reason == null || reason.isBlank() || reason.length() > 1000) throw new BadRequestException("reason is required and must be at most 1000 characters");
    }
    private LeaveRequest visible(String username, UUID id) { User user = user(username); return user.getRole() == Role.EMPLOYEE ? requests.findByIdAndEmployee(id, user).orElseThrow(() -> new ResourceNotFoundException("Leave request not found")) : requests.findByIdAndApprover(id, user).orElseThrow(() -> new ResourceNotFoundException("Leave request not found")); }
    private void validateDates(LocalDate start, LocalDate end, User employee, UUID excluded) { if (end.isBefore(start)) throw new BadRequestException("endDate must not precede startDate"); if (start.isBefore(LocalDate.now(clock))) throw new ConflictException("Leave cannot start in the past"); if (requests.existsOverlapping(employee, ACTIVE, start, end, excluded == null ? UUID.randomUUID() : excluded)) throw new ConflictException("Leave overlaps an existing pending or approved request"); }
    private Attachment attachment(MultipartFile file) { if (file == null || file.isEmpty()) return null; if (file.getSize() > maxAttachmentBytes) throw new BadRequestException("Attachment exceeds the configured size limit"); if (!List.of("application/pdf", "image/jpeg", "image/png").contains(file.getContentType())) throw new BadRequestException("Attachment must be PDF, JPG, or PNG"); try { return new Attachment(file.getOriginalFilename(), file.getContentType(), file.getBytes()); } catch (IOException exception) { throw new BadRequestException("Attachment could not be read"); } }
    private void afterCommit(Consumer<LeaveRequest> action, LeaveRequest request) { if (TransactionSynchronizationManager.isSynchronizationActive()) TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() { public void afterCommit() { action.accept(request); } }); else action.accept(request); }
    private LeaveRequestSummary summary(LeaveRequest r) { return new LeaveRequestSummary(r.getId(), r.getEmployee().getUsername(), r.getLeaveType(), r.getStartDate(), r.getEndDate(), r.getStatus()); }
    private LeaveRequestResponse response(LeaveRequest r) { Attachment a = r.getAttachment(); return new LeaveRequestResponse(r.getId(), r.getEmployee().getUsername(), r.getApprover().getUsername(), r.getLeaveType(), r.getStartDate(), r.getEndDate(), r.getReason(), r.getPartialDay(), a == null ? null : new AttachmentResponse(a.getId(), a.getOriginalFilename(), a.getContentType(), a.getSize()), r.getStatus(), r.getSubmittedAt(), r.getUpdatedAt(), r.getDecidedAt(), r.getDecidedBy() == null ? null : r.getDecidedBy().getUsername(), r.getDecisionComment()); }
}