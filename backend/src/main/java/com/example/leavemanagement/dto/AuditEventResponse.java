package com.example.leavemanagement.dto;

import com.example.leavemanagement.entity.AuditEventType;
import java.time.Instant;
import java.util.UUID;

public record AuditEventResponse(UUID id, AuditEventType eventType, String actor, Instant eventTime, String comment) { }