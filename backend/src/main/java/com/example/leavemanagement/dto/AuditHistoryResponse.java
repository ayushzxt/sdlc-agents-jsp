package com.example.leavemanagement.dto;

import java.util.List;

public record AuditHistoryResponse(List<AuditEventResponse> items) { }