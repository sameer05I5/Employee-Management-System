package com.employeesphere.repository;

import com.employeesphere.entity.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {
    List<Notification> findByEmployeeIdOrderByCreatedAtDesc(Long employeeId);
    List<Notification> findByEmployeeIdAndReadStatusOrderByCreatedAtDesc(Long employeeId, boolean readStatus);
    long countByEmployeeIdAndReadStatus(Long employeeId, boolean readStatus);
}
