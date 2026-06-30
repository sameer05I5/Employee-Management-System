package com.employeesphere.repository;

import com.employeesphere.entity.Attendance;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface AttendanceRepository extends JpaRepository<Attendance, Long> {
    Optional<Attendance> findByEmployeeIdAndDate(Long employeeId, LocalDate date);
    List<Attendance> findByEmployeeIdOrderByDateDesc(Long employeeId);
    List<Attendance> findByDate(LocalDate date);
    long countByDateAndStatus(LocalDate date, Attendance.AttendanceStatus status);
    List<Attendance> findByEmployeeIdAndDateBetween(Long employeeId, LocalDate startDate, LocalDate endDate);
    List<Attendance> findByDateBetween(LocalDate startDate, LocalDate endDate);
}
