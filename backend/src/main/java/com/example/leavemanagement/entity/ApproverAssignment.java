package com.example.leavemanagement.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.util.UUID;

@Entity
@Table(name = "approver_assignments", uniqueConstraints = @UniqueConstraint(name = "uk_assignment_employee", columnNames = "employee_id"))
public class ApproverAssignment {
    @Id @GeneratedValue(strategy = GenerationType.UUID) private UUID id;
    @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "employee_id", nullable = false) private User employee;
    @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "approver_id", nullable = false) private User approver;
    private boolean active = true;

    protected ApproverAssignment() { }
    public ApproverAssignment(User employee, User approver) { this.employee = employee; this.approver = approver; }
    public User getEmployee() { return employee; }
    public User getApprover() { return approver; }
    public boolean isActive() { return active; }
}