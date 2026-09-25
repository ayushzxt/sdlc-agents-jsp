package com.example.leavemanagement.dto;

import com.example.leavemanagement.entity.LeaveRequestStatus;
import com.example.leavemanagement.entity.PartialDay;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public record LeaveRequestResponse(UUID id, String employee, String approver, String leaveType, LocalDate startDate,
                                   LocalDate endDate, String reason, PartialDay partialDay, AttachmentResponse attachment,
                                   LeaveRequestStatus status, Instant submittedAt, Instant updatedAt, Instant decidedAt,
                                   String decidedBy, String decisionComment) { }