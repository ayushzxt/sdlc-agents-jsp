package com.example.leavemanagement.controller;

import com.example.leavemanagement.dto.DecisionRequest;
import com.example.leavemanagement.dto.LeaveRequestResponse;
import com.example.leavemanagement.dto.RejectionRequest;
import com.example.leavemanagement.service.LeaveRequestService;
import jakarta.validation.Valid;
import java.util.UUID;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/approvals/leave-requests")
public class ApprovalController {
    private final LeaveRequestService service;
    public ApprovalController(LeaveRequestService service) { this.service = service; }
    @PostMapping("/{id}/approve")
    public LeaveRequestResponse approve(Authentication auth, @PathVariable UUID id, @Valid @RequestBody(required = false) DecisionRequest request) { return service.approve(auth.getName(), id, request == null ? null : request.comment()); }
    @PostMapping("/{id}/reject")
    public LeaveRequestResponse reject(Authentication auth, @PathVariable UUID id, @Valid @RequestBody(required = false) RejectionRequest request) { return service.reject(auth.getName(), id, request == null ? null : request.comment()); }
}