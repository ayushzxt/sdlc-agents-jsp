package com.example.leavemanagement.repository;

import com.example.leavemanagement.entity.ApproverAssignment;
import com.example.leavemanagement.entity.User;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ApproverAssignmentRepository extends JpaRepository<ApproverAssignment, UUID> {
    Optional<ApproverAssignment> findByEmployeeAndActiveTrue(User employee);
    boolean existsByEmployeeAndActiveTrue(User employee);
}