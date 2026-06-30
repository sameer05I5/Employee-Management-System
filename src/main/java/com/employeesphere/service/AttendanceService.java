package com.employeesphere.service;

import com.employeesphere.dto.AttendanceDto;

import java.time.LocalDate;
import java.util.List;

public interface AttendanceService {
    AttendanceDto checkIn(Long employeeId);
    AttendanceDto checkOut(Long employeeId);
    AttendanceDto getTodayAttendance(Long employeeId);
    List<AttendanceDto> getAttendanceHistory(Long employeeId);
    List<AttendanceDto> getAttendanceByDate(LocalDate date);
    List<AttendanceDto> getAttendanceReport(Long employeeId, LocalDate start, LocalDate end);
    long getPresentCount(LocalDate date);
    long getAbsentCount(LocalDate date);
    long getLateCount(LocalDate date);
    List<AttendanceDto> getAllAttendance();
}
