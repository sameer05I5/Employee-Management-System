package com.employeesphere.service;

import com.employeesphere.entity.Notification;

import java.util.List;

public interface NotificationService {
    void sendNotification(Long employeeId, String title, String message);
    List<Notification> getNotificationsForEmployee(Long employeeId);
    List<Notification> getUnreadNotificationsForEmployee(Long employeeId);
    void markAsRead(Long notificationId);
    void markAllAsRead(Long employeeId);
    long getUnreadCount(Long employeeId);
}
