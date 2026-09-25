package com.example.leavemanagement.entity;

import jakarta.persistence.Column;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "leave_requests", indexes = {
    @Index(name = "idx_leave_employee_status_dates", columnList = "employee_id,status,start_date,end_date"),
    @Index(name = "idx_leave_approver_status_date", columnList = "approver_id,status,start_date")
})
public class LeaveRequest {
    @Id private UUID id;
    @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "employee_id", nullable = false) private User employee;
    @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "approver_id", nullable = false) private User approver;
    @Column(name = "leave_type", nullable = false, length = 50) private String leaveType;
    @Column(name = "start_date", nullable = false) private LocalDate startDate;
    @Column(name = "end_date", nullable = false) private LocalDate endDate;
    @Column(nullable = false, length = 1000) private String reason;
    @Enumerated(EnumType.STRING) @Column(length = 20) private PartialDay partialDay;
    @OneToOne(fetch = FetchType.LAZY, cascade = {CascadeType.PERSIST, CascadeType.MERGE}) @JoinColumn(name = "attachment_id") private Attachment attachment;
    @Enumerated(EnumType.STRING) @Column(nullable = false, length = 20) private LeaveRequestStatus status;
    @Column(nullable = false) private Instant submittedAt;
    @Column(nullable = false) private Instant updatedAt;
    private Instant decidedAt;
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "decided_by") private User decidedBy;
    @Column(length = 1000) private String decisionComment;
    @Version private long version;

    protected LeaveRequest() { }
    public LeaveRequest(User employee, User approver, String leaveType, LocalDate startDate, LocalDate endDate,
                        String reason, PartialDay partialDay, Attachment attachment, Instant now) {
        this.id = UUID.randomUUID(); this.employee = employee; this.approver = approver; this.leaveType = leaveType;
        this.startDate = startDate; this.endDate = endDate; this.reason = reason; this.partialDay = partialDay;
        this.attachment = attachment; this.status = LeaveRequestStatus.PENDING; this.submittedAt = now; this.updatedAt = now;
    }
    public void update(String leaveType, LocalDate startDate, LocalDate endDate, String reason, PartialDay partialDay, Attachment attachment, Instant now) {
        this.leaveType = leaveType; this.startDate = startDate; this.endDate = endDate; this.reason = reason; this.partialDay = partialDay;
        if (attachment != null) this.attachment = attachment; this.updatedAt = now;
    }
    public void decide(LeaveRequestStatus newStatus, User actor, String comment, Instant now) {
        this.status = newStatus; this.decidedBy = actor; this.decisionComment = comment; this.decidedAt = now; this.updatedAt = now;
    }
    public void cancel(Instant now) { this.status = LeaveRequestStatus.CANCELLED; this.updatedAt = now; }
    public UUID getId() { return id; } public User getEmployee() { return employee; } public User getApprover() { return approver; }
    public String getLeaveType() { return leaveType; } public LocalDate getStartDate() { return startDate; } public LocalDate getEndDate() { return endDate; }
    public String getReason() { return reason; } public PartialDay getPartialDay() { return partialDay; } public Attachment getAttachment() { return attachment; }
    public LeaveRequestStatus getStatus() { return status; } public Instant getSubmittedAt() { return submittedAt; } public Instant getUpdatedAt() { return updatedAt; }
    public Instant getDecidedAt() { return decidedAt; } public User getDecidedBy() { return decidedBy; } public String getDecisionComment() { return decisionComment; }
}