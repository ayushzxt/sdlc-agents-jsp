package com.example.leavemanagement.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "audit_events", indexes = @Index(name = "idx_audit_request_time", columnList = "request_id,event_time"))
public class AuditEvent {
    @Id @GeneratedValue(strategy = GenerationType.UUID) private UUID id;
    @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "request_id", nullable = false) private LeaveRequest request;
    @Enumerated(EnumType.STRING) @Column(nullable = false, length = 20) private AuditEventType eventType;
    @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "actor_id", nullable = false) private User actor;
    @Column(name = "event_time", nullable = false) private Instant eventTime;
    @Column(length = 1000) private String comment;

    protected AuditEvent() { }
    public AuditEvent(LeaveRequest request, AuditEventType eventType, User actor, Instant eventTime, String comment) {
        this.request = request; this.eventType = eventType; this.actor = actor; this.eventTime = eventTime; this.comment = comment;
    }
    public UUID getId() { return id; } public LeaveRequest getRequest() { return request; } public AuditEventType getEventType() { return eventType; }
    public User getActor() { return actor; } public Instant getEventTime() { return eventTime; } public String getComment() { return comment; }
}