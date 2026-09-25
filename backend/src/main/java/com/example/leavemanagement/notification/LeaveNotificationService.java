package com.example.leavemanagement.notification;

import com.example.leavemanagement.entity.LeaveRequest;

public interface LeaveNotificationService {
    void submitted(LeaveRequest request);
    void approved(LeaveRequest request);
    void rejected(LeaveRequest request);
    void cancelled(LeaveRequest request);
}