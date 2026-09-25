package com.example.leavemanagement.dto;

import java.util.List;

public record LeaveRequestListResponse(List<LeaveRequestSummary> items) { }