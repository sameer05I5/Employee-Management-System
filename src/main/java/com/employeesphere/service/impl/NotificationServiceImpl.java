package com.employeesphere.service.impl;

import com.employeesphere.entity.Employee;
import com.employeesphere.entity.Notification;
import com.employeesphere.exception.ResourceNotFoundException;
import com.employeesphere.repository.EmployeeRepository;
import com.employeesphere.repository.NotificationRepository;
import com.employeesphere.service.NotificationService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class NotificationServiceImpl implements NotificationService {

    private final NotificationRepository notificationRepository;
    private final EmployeeRepository employeeRepository;

    public NotificationServiceImpl(NotificationRepository notificationRepository, EmployeeRepository employeeRepository) {
        this.notificationRepository = notificationRepository;
        this.employeeRepository = employeeRepository;
    }

    @Override
    public void sendNotification(Long employeeId, String title, String message) {
        Employee employee = null;
        if (employeeId != null) {
            employee = employeeRepository.findById(employeeId)
                    .orElseThrow(() -> new ResourceNotFoundException("Employee not found with id: " + employeeId));
        }

        Notification notification = Notification.builder()
                .employee(employee)
                .title(title)
                .message(message)
                .build();

        notificationRepository.save(notification);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Notification> getNotificationsForEmployee(Long employeeId) {
        return notificationRepository.findByEmployeeIdOrderByCreatedAtDesc(employeeId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Notification> getUnreadNotificationsForEmployee(Long employeeId) {
        return notificationRepository.findByEmployeeIdAndReadStatusOrderByCreatedAtDesc(employeeId, false);
    }

    @Override
    public void markAsRead(Long notificationId) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new ResourceNotFoundException("Notification not found with id: " + notificationId));
        notification.setReadStatus(true);
        notificationRepository.save(notification);
    }

    @Override
    public void markAllAsRead(Long employeeId) {
        List<Notification> unread = notificationRepository.findByEmployeeIdAndReadStatusOrderByCreatedAtDesc(employeeId, false);
        for (Notification n : unread) {
            n.setReadStatus(true);
        }
        notificationRepository.saveAll(unread);
    }

    @Override
    @Transactional(readOnly = true)
    public long getUnreadCount(Long employeeId) {
        return notificationRepository.countByEmployeeIdAndReadStatus(employeeId, false);
    }
}
