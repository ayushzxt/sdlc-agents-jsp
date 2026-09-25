package com.example.leavemanagement.notification;

import com.example.leavemanagement.entity.LeaveRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailLeaveNotificationService implements LeaveNotificationService {
    private static final Logger logger = LoggerFactory.getLogger(EmailLeaveNotificationService.class);
    private final JavaMailSender mailSender;
    private final String from;
    public EmailLeaveNotificationService(JavaMailSender mailSender, org.springframework.core.env.Environment environment) {
        this.mailSender = mailSender; this.from = environment.getProperty("leave-management.notification-from", "no-reply@example.invalid");
    }
    public void submitted(LeaveRequest request) { send(request, "Leave request submitted"); }
    public void approved(LeaveRequest request) { send(request, "Leave request approved"); }
    public void rejected(LeaveRequest request) { send(request, "Leave request rejected"); }
    public void cancelled(LeaveRequest request) { send(request, "Leave request cancelled"); }
    private void send(LeaveRequest request, String subject) {
        try {
            SimpleMailMessage message = new SimpleMailMessage(); message.setFrom(from); message.setTo(request.getEmployee().getUsername());
            message.setSubject(subject); message.setText("Leave request " + request.getId() + " is " + request.getStatus() + "."); mailSender.send(message);
        } catch (RuntimeException exception) { logger.warn("Leave notification delivery failed for request {}", request.getId()); }
    }
}