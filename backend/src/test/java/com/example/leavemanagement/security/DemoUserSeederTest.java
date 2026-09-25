package com.example.leavemanagement.security;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.leavemanagement.entity.Role;
import com.example.leavemanagement.repository.ApproverAssignmentRepository;
import com.example.leavemanagement.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;

@SpringBootTest
class DemoUserSeederTest {
    @Autowired private UserRepository users;
    @Autowired private ApproverAssignmentRepository assignments;
    @Autowired private PasswordEncoder passwordEncoder;

    @Test
    void seedsDemoEmployeeAndApproverAccounts() {
        var employee = users.findByUsername("employee@example.com");
        assertThat(employee).isPresent();
        assertThat(employee.get().getRole()).isEqualTo(Role.EMPLOYEE);
        assertThat(passwordEncoder.matches("Password123!", employee.get().getPasswordHash())).isTrue();

        var approver = users.findByUsername("approver@example.com");
        assertThat(approver).isPresent();
        assertThat(approver.get().getRole()).isEqualTo(Role.APPROVER);
        assertThat(passwordEncoder.matches("Password123!", approver.get().getPasswordHash())).isTrue();

        assertThat(assignments.findByEmployeeAndActiveTrue(employee.get())).isPresent();
    }
}
