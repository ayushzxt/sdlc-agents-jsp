package com.example.leavemanagement.security;

import com.example.leavemanagement.entity.ApproverAssignment;
import com.example.leavemanagement.entity.Role;
import com.example.leavemanagement.entity.User;
import com.example.leavemanagement.repository.ApproverAssignmentRepository;
import com.example.leavemanagement.repository.UserRepository;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class DemoUserSeeder implements ApplicationRunner {
    private static final String DEMO_PASSWORD = "Password123!";

    private final UserRepository users;
    private final ApproverAssignmentRepository assignments;
    private final PasswordEncoder passwordEncoder;

    public DemoUserSeeder(UserRepository users, ApproverAssignmentRepository assignments, PasswordEncoder passwordEncoder) {
        this.users = users;
        this.assignments = assignments;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(ApplicationArguments args) {
        User employee = users.findByUsername("employee@example.com")
            .orElseGet(() -> users.save(new User("employee@example.com", passwordEncoder.encode(DEMO_PASSWORD), Role.EMPLOYEE)));

        User approver = users.findByUsername("approver@example.com")
            .orElseGet(() -> users.save(new User("approver@example.com", passwordEncoder.encode(DEMO_PASSWORD), Role.APPROVER)));

        if (assignments.findByEmployeeAndActiveTrue(employee).isEmpty()) {
            assignments.save(new ApproverAssignment(employee, approver));
        }
    }
}
