package com.example.leavemanagement.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import com.example.leavemanagement.entity.ApproverAssignment;
import com.example.leavemanagement.entity.Attachment;
import com.example.leavemanagement.entity.Role;
import com.example.leavemanagement.entity.User;
import com.example.leavemanagement.notification.LeaveNotificationService;
import com.example.leavemanagement.security.CurrentUserService;
import com.example.leavemanagement.service.AuditService;
import com.example.leavemanagement.service.impl.LeaveRequestServiceImpl;
import java.time.LocalDate;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.mock.web.MockMultipartFile;

@DataJpaTest
@Import({LeaveRequestServiceImpl.class, AuditService.class})
class AttachmentPersistenceTest {
    @Autowired private UserRepository users;
    @Autowired private ApproverAssignmentRepository assignments;
    @Autowired private LeaveRequestRepository requests;
    @Autowired private jakarta.persistence.EntityManager entityManager;
    @MockBean private CurrentUserService currentUser;
    @MockBean private LeaveNotificationService notifications;

    @Test
    void persistsMultipartAttachmentThroughLeaveRequestCascade() {
        User employee = users.save(new User("employee@example.com", "hash", Role.EMPLOYEE));
        User approver = users.save(new User("approver@example.com", "hash", Role.APPROVER));
        assignments.save(new ApproverAssignment(employee, approver));
        LeaveRequestServiceImpl service = new LeaveRequestServiceImpl(users, assignments, requests,
            new AuditService(mock(AuditEventRepository.class)), currentUser, notifications, 5_000_000);

        var response = service.create(employee.getUsername(),
            new com.example.leavemanagement.dto.LeaveRequestCreateRequest("Annual", LocalDate.now().plusDays(1),
                LocalDate.now().plusDays(2), "Rest", null),
            new MockMultipartFile("attachment", "proof.pdf", "application/pdf", "pdf-bytes".getBytes()));

        entityManager.flush();
        Attachment stored = entityManager.find(Attachment.class, response.attachment().id());
        assertThat(stored).isNotNull();
        assertThat(stored.getContent()).isEqualTo("pdf-bytes".getBytes());
    }
}