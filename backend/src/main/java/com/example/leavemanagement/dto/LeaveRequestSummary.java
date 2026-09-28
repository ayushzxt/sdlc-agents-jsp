package com.example.leavemanagement.dto;

import com.example.leavemanagement.entity.LeaveRequestStatus;
import java.time.LocalDate;
import java.util.UUID;

public record LeaveRequestSummary(UUID id, String employee, String leaveType, LocalDate startDate, LocalDate endDate, LeaveRequestStatus status) { }