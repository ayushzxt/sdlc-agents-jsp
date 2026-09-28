package com.example.leavemanagement.service;

import com.example.leavemanagement.entity.AuditEvent;
import com.example.leavemanagement.entity.AuditEventType;
import com.example.leavemanagement.entity.LeaveRequest;
import com.example.leavemanagement.entity.User;
import com.example.leavemanagement.repository.AuditEventRepository;
import java.time.Instant;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class AuditService {
    private final AuditEventRepository repository;
    public AuditService(AuditEventRepository repository) { this.repository = repository; }
    public void record(LeaveRequest request, AuditEventType type, User actor, String comment, Instant now) { repository.save(new AuditEvent(request, type, actor, now, comment)); }
    public List<AuditEvent> history(LeaveRequest request) { return repository.findByRequestIdOrderByEventTimeAsc(request.getId()); }
}