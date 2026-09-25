package com.example.leavemanagement.repository;

import com.example.leavemanagement.entity.AuditEvent;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AuditEventRepository extends JpaRepository<AuditEvent, UUID> { List<AuditEvent> findByRequestIdOrderByEventTimeAsc(UUID requestId); }