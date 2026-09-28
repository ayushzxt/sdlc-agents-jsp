package com.example.leavemanagement.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.example.leavemanagement.dto.LeaveRequestCreateRequest;
import com.example.leavemanagement.dto.LeaveRequestUpdateRequest;
import com.example.leavemanagement.entity.ApproverAssignment;
import com.example.leavemanagement.entity.AuditEvent;
import com.example.leavemanagement.entity.AuditEventType;
import com.example.leavemanagement.entity.LeaveRequest;
import com.example.leavemanagement.entity.LeaveRequestStatus;
import com.example.leavemanagement.entity.Role;
import com.example.leavemanagement.entity.User;
import com.example.leavemanagement.exception.BadRequestException;
import com.example.leavemanagement.exception.ConflictException;
import com.example.leavemanagement.exception.ForbiddenException;
import com.example.leavemanagement.notification.LeaveNotificationService;
import com.example.leavemanagement.repository.ApproverAssignmentRepository;
import com.example.leavemanagement.repository.AuditEventRepository;
import com.example.leavemanagement.repository.LeaveRequestRepository;
import com.example.leavemanagement.repository.UserRepository;
import com.example.leavemanagement.security.CurrentUserService;
import com.example.leavemanagement.service.impl.LeaveRequestServiceImpl;
import java.time.LocalDate;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicBoolean;
import org.mockito.ArgumentCaptor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.multipart.MultipartFile;

@ExtendWith(MockitoExtension.class)
class LeaveRequestServiceImplTest {
    @Mock UserRepository users; @Mock ApproverAssignmentRepository assignments; @Mock LeaveRequestRepository requests;
    @Mock AuditEventRepository auditEvents; @Mock CurrentUserService currentUser; @Mock LeaveNotificationService notifications; @Mock MultipartFile file;
    private LeaveRequestServiceImpl service; private User employee; private User approver;

    @BeforeEach
    void setUp() {
        employee = new User("employee@example.com", "hash", Role.EMPLOYEE); approver = new User("approver@example.com", "hash", Role.APPROVER);
        service = new LeaveRequestServiceImpl(users, assignments, requests, new AuditService(auditEvents), currentUser, notifications, 5_000_000);
        lenient().when(users.findByUsername(employee.getUsername())).thenReturn(Optional.of(employee));
        lenient().when(assignments.findByEmployeeAndActiveTrue(employee)).thenReturn(Optional.of(new ApproverAssignment(employee, approver)));
        lenient().when(requests.existsOverlapping(any(), any(), any(), any(), any())).thenReturn(false);
        lenient().when(requests.save(any(LeaveRequest.class))).thenAnswer(invocation -> invocation.getArgument(0));
    }

    @Test
    void createsPendingRequestWithServerOwnedApproverAuditAndNotification() {
        var response = service.create(employee.getUsername(), createRequest(), null);

        assertThat(response.status()).isEqualTo(LeaveRequestStatus.PENDING);
        assertThat(response.employee()).isEqualTo(employee.getUsername());
        assertThat(response.approver()).isEqualTo(approver.getUsername());
        ArgumentCaptor<AuditEvent> audit = ArgumentCaptor.forClass(AuditEvent.class);
        verify(auditEvents).save(audit.capture());
        assertThat(audit.getValue().getEventType()).isEqualTo(AuditEventType.CREATED);
        assertThat(audit.getValue().getActor()).isSameAs(employee);
        verify(notifications).submitted(any(LeaveRequest.class));
    }

    @Test
    void rejectsPastDate() {
        assertThatThrownBy(() -> service.create(employee.getUsername(), new LeaveRequestCreateRequest("Annual", LocalDate.now().minusDays(1), LocalDate.now(), "Rest", null), null))
            .isInstanceOf(ConflictException.class).hasMessageContaining("past");
        verify(requests, never()).save(any());
    }

    @Test
    void rejectsOverlap() {
        when(requests.existsOverlapping(any(), any(), any(), any(), any())).thenReturn(true);
        assertThatThrownBy(() -> service.create(employee.getUsername(), createRequest(), null)).isInstanceOf(ConflictException.class);
        verify(requests, never()).save(any());
    }

    @Test
    void acceptsSameDayLeaveAndRejectsReversedDates() {
        var response = service.create(employee.getUsername(), new LeaveRequestCreateRequest("Annual", LocalDate.now().plusDays(1), LocalDate.now().plusDays(1), "Rest", null), null);
        assertThat(response.startDate()).isEqualTo(response.endDate());

        assertThatThrownBy(() -> service.create(employee.getUsername(), new LeaveRequestCreateRequest("Annual", LocalDate.now().plusDays(2), LocalDate.now().plusDays(1), "Rest", null), null))
            .isInstanceOf(BadRequestException.class).hasMessageContaining("endDate");
    }

    @Test
    void rejectsMissingRequiredFieldsAndDoesNotPersist() {
        assertThatThrownBy(() -> service.create(employee.getUsername(), new LeaveRequestCreateRequest(" ", LocalDate.now().plusDays(1), LocalDate.now().plusDays(1), "Rest", null), null)).isInstanceOf(BadRequestException.class);
        assertThatThrownBy(() -> service.create(employee.getUsername(), new LeaveRequestCreateRequest("Annual", null, LocalDate.now().plusDays(1), "Rest", null), null)).isInstanceOf(BadRequestException.class);
        assertThatThrownBy(() -> service.create(employee.getUsername(), new LeaveRequestCreateRequest("Annual", LocalDate.now().plusDays(1), LocalDate.now().plusDays(1), " ", null), null)).isInstanceOf(BadRequestException.class);
        verify(requests, never()).save(any());
    }

    @Test
    void rejectsUnsupportedAndOversizedAttachments() {
        when(file.isEmpty()).thenReturn(false);
        when(file.getContentType()).thenReturn("text/plain", "application/pdf");
        assertThatThrownBy(() -> service.create(employee.getUsername(), createRequest(), file)).isInstanceOf(BadRequestException.class).hasMessageContaining("PDF");

        when(file.getSize()).thenReturn(5_001L);
        LeaveRequestServiceImpl limited = new LeaveRequestServiceImpl(users, assignments, requests, new AuditService(auditEvents), currentUser, notifications, 5_000);
        assertThatThrownBy(() -> limited.create(employee.getUsername(), createRequest(), file)).isInstanceOf(BadRequestException.class).hasMessageContaining("size");
    }

    @Test
    void rejectsApproverFromSubmittingAsEmployee() {
        when(users.findByUsername(approver.getUsername())).thenReturn(Optional.of(approver));
        assertThatThrownBy(() -> service.create(approver.getUsername(), createRequest(), null)).isInstanceOf(ForbiddenException.class);
    }

    @Test
    void rejectsBlankRejectionComment() {
        assertThatThrownBy(() -> service.reject(approver.getUsername(), UUID.randomUUID(), " ")).isInstanceOf(BadRequestException.class);
    }

    @Test
    void listsEmployeesOwnRequestsAndRejectsEmployeeFilters() {
        LeaveRequest request = request(LeaveRequestStatus.PENDING);
        when(requests.findByEmployeeOrderBySubmittedAtDesc(employee)).thenReturn(List.of(request));

        assertThat(service.list(employee.getUsername(), null, null, null).items()).hasSize(1);
        assertThatThrownBy(() -> service.list(employee.getUsername(), LeaveRequestStatus.PENDING, null, null)).isInstanceOf(BadRequestException.class);
        verify(requests).findByEmployeeOrderBySubmittedAtDesc(employee);
        verify(requests, never()).findByApproverAndStatusInOrderBySubmittedAtDesc(any(), any());
    }

    @Test
    void listsApproverAssignedStatusesAndFiltersInvalidDateRange() {
        when(users.findByUsername(approver.getUsername())).thenReturn(Optional.of(approver));
        when(requests.findByApproverAndStatusInAndOptionalDateRangeOrderBySubmittedAtDesc(eq(approver), any(), any(), any())).thenReturn(List.of(request(LeaveRequestStatus.PENDING)));

        assertThat(service.list(approver.getUsername(), LeaveRequestStatus.PENDING, null, null).items()).hasSize(1);
        assertThatThrownBy(() -> service.list(approver.getUsername(), null, LocalDate.now().plusDays(3), LocalDate.now().plusDays(2))).isInstanceOf(BadRequestException.class);
    }

    @Test
    void approvesPendingRequestWithOptionalCommentAndRecordsDecision() {
        LeaveRequest request = request(LeaveRequestStatus.PENDING);
        when(users.findByUsername(approver.getUsername())).thenReturn(Optional.of(approver));
        when(requests.findByIdAndApprover(request.getId(), approver)).thenReturn(Optional.of(request));
        when(requests.decideIfPending(any(), any(), any(), any(), any(), any(), any())).thenAnswer(invocation -> {
            request.decide(invocation.getArgument(2), approver, invocation.getArgument(4), invocation.getArgument(5));
            return 1;
        });

        var response = service.approve(approver.getUsername(), request.getId(), "Looks good");

        assertThat(response.status()).isEqualTo(LeaveRequestStatus.APPROVED);
        assertThat(response.decidedBy()).isEqualTo(approver.getUsername());
        assertThat(response.decisionComment()).isEqualTo("Looks good");
        verify(notifications).approved(request);
        verify(auditEvents).save(any(AuditEvent.class));
    }

    @Test
    void rejectsPendingRequestWithRequiredCommentAndNotifies() {
        LeaveRequest request = request(LeaveRequestStatus.PENDING);
        when(users.findByUsername(approver.getUsername())).thenReturn(Optional.of(approver));
        when(requests.findByIdAndApprover(request.getId(), approver)).thenReturn(Optional.of(request));
        when(requests.decideIfPending(any(), any(), any(), any(), any(), any(), any())).thenAnswer(invocation -> {
            request.decide(invocation.getArgument(2), approver, invocation.getArgument(4), invocation.getArgument(5));
            return 1;
        });

        var response = service.reject(approver.getUsername(), request.getId(), "Insufficient coverage");

        assertThat(response.status()).isEqualTo(LeaveRequestStatus.REJECTED);
        assertThat(response.decisionComment()).isEqualTo("Insufficient coverage");
        verify(notifications).rejected(request);
    }

    @Test
    void preventsUnauthorizedAndSecondApprovalDecisions() {
        LeaveRequest request = request(LeaveRequestStatus.PENDING);
        when(users.findByUsername(employee.getUsername())).thenReturn(Optional.of(employee));
        assertThatThrownBy(() -> service.approve(employee.getUsername(), request.getId(), null)).isInstanceOf(ForbiddenException.class);

        when(users.findByUsername(approver.getUsername())).thenReturn(Optional.of(approver));
        when(requests.findByIdAndApprover(request.getId(), approver)).thenReturn(Optional.of(request));
        when(requests.decideIfPending(any(), any(), any(), any(), any(), any(), any())).thenAnswer(invocation -> {
            request.decide(invocation.getArgument(2), approver, invocation.getArgument(4), invocation.getArgument(5));
            return 1;
        });
        service.approve(approver.getUsername(), request.getId(), null);
        assertThatThrownBy(() -> service.reject(approver.getUsername(), request.getId(), "Too late")).isInstanceOf(ConflictException.class);
        assertThat(request.getStatus()).isEqualTo(LeaveRequestStatus.APPROVED);
    }

    @Test
    void concurrentApprovalAndRejectionHaveOneWinnerAndOneConflict() throws Exception {
        LeaveRequest request = request(LeaveRequestStatus.PENDING);
        when(users.findByUsername(approver.getUsername())).thenReturn(Optional.of(approver));
        when(requests.findByIdAndApprover(request.getId(), approver)).thenReturn(Optional.of(request));
        AtomicBoolean won = new AtomicBoolean();
        when(requests.decideIfPending(any(), any(), any(), any(), any(), any(), any())).thenAnswer(invocation -> {
            if (!won.compareAndSet(false, true)) return 0;
            request.decide(invocation.getArgument(2), approver, invocation.getArgument(4), invocation.getArgument(5));
            return 1;
        });

        ExecutorService executor = Executors.newFixedThreadPool(2);
        Future<?> approval = executor.submit(() -> service.approve(approver.getUsername(), request.getId(), "approved"));
        Future<?> rejection = executor.submit(() -> service.reject(approver.getUsername(), request.getId(), "rejected"));
        int conflicts = 0;
        for (Future<?> result : List.of(approval, rejection)) {
            try { result.get(); } catch (java.util.concurrent.ExecutionException exception) {
                if (exception.getCause() instanceof ConflictException) conflicts++;
                else throw exception;
            }
        }
        executor.shutdownNow();

        assertThat(conflicts).isEqualTo(1);
        assertThat(request.getStatus()).isIn(LeaveRequestStatus.APPROVED, LeaveRequestStatus.REJECTED);
    }

    @Test
    void updatesOnlyPendingAndCancelsPendingOrApprovedButNotTerminalRequests() {
        LeaveRequest pending = request(LeaveRequestStatus.PENDING);
        when(requests.findByIdAndEmployee(pending.getId(), employee)).thenReturn(Optional.of(pending));
        var updated = service.update(employee.getUsername(), pending.getId(), new LeaveRequestUpdateRequest("Updated", LocalDate.now().plusDays(2), LocalDate.now().plusDays(3), "Changed", null), null);
        assertThat(updated.leaveType()).isEqualTo("Updated");
        verify(notifications).submitted(pending);

        LeaveRequest approved = request(LeaveRequestStatus.APPROVED);
        when(requests.findByIdAndEmployee(approved.getId(), employee)).thenReturn(Optional.of(approved));
        assertThat(service.cancel(employee.getUsername(), approved.getId()).status()).isEqualTo(LeaveRequestStatus.CANCELLED);
        verify(notifications).cancelled(approved);

        LeaveRequest rejected = request(LeaveRequestStatus.REJECTED);
        when(requests.findByIdAndEmployee(rejected.getId(), employee)).thenReturn(Optional.of(rejected));
        assertThatThrownBy(() -> service.update(employee.getUsername(), rejected.getId(), new LeaveRequestUpdateRequest("No", LocalDate.now().plusDays(2), LocalDate.now().plusDays(2), "No", null), null)).isInstanceOf(ConflictException.class);
        assertThatThrownBy(() -> service.cancel(employee.getUsername(), rejected.getId())).isInstanceOf(ConflictException.class);
    }

    @Test
    void usesEmployeeScopedRepositoriesForDetailAndHistory() {
        LeaveRequest request = request(LeaveRequestStatus.PENDING);
        when(requests.findByIdAndEmployee(request.getId(), employee)).thenReturn(Optional.of(request));
        when(auditEvents.findByRequestIdOrderByEventTimeAsc(request.getId())).thenReturn(List.of());

        service.detail(employee.getUsername(), request.getId());
        service.history(employee.getUsername(), request.getId());

        verify(requests, org.mockito.Mockito.times(2)).findByIdAndEmployee(request.getId(), employee);
        verify(auditEvents).findByRequestIdOrderByEventTimeAsc(request.getId());
    }

    private LeaveRequestCreateRequest createRequest() {
        return new LeaveRequestCreateRequest("Annual", LocalDate.now().plusDays(1), LocalDate.now().plusDays(2), "Rest", null);
    }

    private LeaveRequest request(LeaveRequestStatus status) {
        LeaveRequest request = new LeaveRequest(employee, approver, "Annual", LocalDate.now().plusDays(1), LocalDate.now().plusDays(2), "Rest", null, null, Instant.now());
        if (status == LeaveRequestStatus.APPROVED || status == LeaveRequestStatus.REJECTED) request.decide(status, approver, "Decision", Instant.now());
        if (status == LeaveRequestStatus.CANCELLED) request.cancel(Instant.now());
        return request;
    }
}