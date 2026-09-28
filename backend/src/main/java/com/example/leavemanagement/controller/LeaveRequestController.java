package com.example.leavemanagement.controller;

import com.example.leavemanagement.dto.*;
import com.example.leavemanagement.entity.LeaveRequestStatus;
import com.example.leavemanagement.entity.PartialDay;
import com.example.leavemanagement.service.LeaveRequestService;
import jakarta.validation.Valid;
import java.time.LocalDate;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.util.UriComponentsBuilder;

@RestController
@RequestMapping("/api/leave-requests")
public class LeaveRequestController {
    private final LeaveRequestService service;
    public LeaveRequestController(LeaveRequestService service) { this.service = service; }

    @PostMapping(consumes = "multipart/form-data")
    public ResponseEntity<LeaveRequestResponse> create(Authentication auth, @RequestParam String leaveType, @RequestParam LocalDate startDate,
                                                        @RequestParam LocalDate endDate, @RequestParam String reason,
                                                        @RequestParam(required = false) PartialDay partialDay,
                                                        @RequestParam(required = false) MultipartFile attachment, UriComponentsBuilder uri) {
        LeaveRequestResponse response = service.create(auth.getName(), new LeaveRequestCreateRequest(leaveType, startDate, endDate, reason, partialDay), attachment);
        return ResponseEntity.created(uri.path("/api/leave-requests/{id}").build(response.id())).body(response);
    }

    @GetMapping
    public LeaveRequestListResponse list(Authentication auth, @RequestParam(required = false) LeaveRequestStatus status,
                                         @RequestParam(required = false) LocalDate startDate, @RequestParam(required = false) LocalDate endDate) { return service.list(auth.getName(), status, startDate, endDate); }

    @GetMapping("/{id}")
    public LeaveRequestResponse detail(Authentication auth, @PathVariable UUID id) { return service.detail(auth.getName(), id); }

    @PatchMapping(value = "/{id}", consumes = "multipart/form-data")
    public LeaveRequestResponse update(Authentication auth, @PathVariable UUID id, @RequestParam String leaveType, @RequestParam LocalDate startDate,
                                       @RequestParam LocalDate endDate, @RequestParam String reason, @RequestParam(required = false) PartialDay partialDay,
                                       @RequestParam(required = false) MultipartFile attachment) { return service.update(auth.getName(), id, new LeaveRequestUpdateRequest(leaveType, startDate, endDate, reason, partialDay), attachment); }

    @PostMapping("/{id}/cancel")
    public LeaveRequestResponse cancel(Authentication auth, @PathVariable UUID id) { return service.cancel(auth.getName(), id); }

    @GetMapping("/{id}/history")
    public AuditHistoryResponse history(Authentication auth, @PathVariable UUID id) { return service.history(auth.getName(), id); }
}