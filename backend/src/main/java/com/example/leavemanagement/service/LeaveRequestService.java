package com.example.leavemanagement.service;

import com.example.leavemanagement.dto.*;
import com.example.leavemanagement.entity.LeaveRequestStatus;
import java.time.LocalDate;
import java.util.UUID;
import org.springframework.web.multipart.MultipartFile;

public interface LeaveRequestService {
    LeaveRequestResponse create(String username, LeaveRequestCreateRequest request, MultipartFile attachment);
    LeaveRequestListResponse list(String username, LeaveRequestStatus status, LocalDate startDate, LocalDate endDate);
    LeaveRequestResponse detail(String username, UUID id);
    LeaveRequestResponse update(String username, UUID id, LeaveRequestUpdateRequest request, MultipartFile attachment);
    LeaveRequestResponse cancel(String username, UUID id);
    AuditHistoryResponse history(String username, UUID id);
    LeaveRequestResponse approve(String username, UUID id, String comment);
    LeaveRequestResponse reject(String username, UUID id, String comment);
}