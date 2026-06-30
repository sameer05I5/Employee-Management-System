package com.employeesphere.service.impl;

import com.employeesphere.dto.AttendanceDto;
import com.employeesphere.entity.Attendance;
import com.employeesphere.entity.Employee;
import com.employeesphere.exception.BadRequestException;
import com.employeesphere.exception.ResourceNotFoundException;
import com.employeesphere.repository.AttendanceRepository;
import com.employeesphere.repository.EmployeeRepository;
import com.employeesphere.service.AttendanceService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional
public class AttendanceServiceImpl implements AttendanceService {

    private final AttendanceRepository attendanceRepository;
    private final EmployeeRepository employeeRepository;

    private static final LocalTime LATE_THRESHOLD = LocalTime.of(9, 30); // Late after 9:30 AM

    public AttendanceServiceImpl(AttendanceRepository attendanceRepository, EmployeeRepository employeeRepository) {
        this.attendanceRepository = attendanceRepository;
        this.employeeRepository = employeeRepository;
    }

    @Override
    public AttendanceDto checkIn(Long employeeId) {
        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found with id: " + employeeId));

        LocalDate today = LocalDate.now();
        Optional<Attendance> existing = attendanceRepository.findByEmployeeIdAndDate(employeeId, today);
        if (existing.isPresent()) {
            throw new BadRequestException("Employee already checked in for today");
        }

        LocalDateTime now = LocalDateTime.now();
        Attendance.AttendanceStatus status = Attendance.AttendanceStatus.PRESENT;
        if (now.toLocalTime().isAfter(LATE_THRESHOLD)) {
            status = Attendance.AttendanceStatus.LATE;
        }

        Attendance attendance = Attendance.builder()
                .employee(employee)
                .date(today)
                .checkIn(now)
                .status(status)
                .build();

        Attendance saved = attendanceRepository.save(attendance);
        return mapToDto(saved);
    }

    @Override
    public AttendanceDto checkOut(Long employeeId) {
        LocalDate today = LocalDate.now();
        Attendance attendance = attendanceRepository.findByEmployeeIdAndDate(employeeId, today)
                .orElseThrow(() -> new BadRequestException("No check-in record found for today. Please check-in first."));

        if (attendance.getCheckOut() != null) {
            throw new BadRequestException("Employee already checked out for today");
        }

        LocalDateTime checkOutTime = LocalDateTime.now();
        attendance.setCheckOut(checkOutTime);

        // Calculate working hours
        Duration duration = Duration.between(attendance.getCheckIn(), checkOutTime);
        double hours = duration.toMinutes() / 60.0;
        // Keep to 2 decimal places
        hours = Math.round(hours * 100.0) / 100.0;
        attendance.setWorkingHours(hours);

        // If working hours < 4, mark as half day (optional business logic)
        if (hours < 4.0) {
            attendance.setStatus(Attendance.AttendanceStatus.HALF_DAY);
        }

        Attendance saved = attendanceRepository.save(attendance);
        return mapToDto(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public AttendanceDto getTodayAttendance(Long employeeId) {
        LocalDate today = LocalDate.now();
        return attendanceRepository.findByEmployeeIdAndDate(employeeId, today)
                .map(this::mapToDto)
                .orElse(null);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AttendanceDto> getAttendanceHistory(Long employeeId) {
        return attendanceRepository.findByEmployeeIdOrderByDateDesc(employeeId).stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<AttendanceDto> getAttendanceByDate(LocalDate date) {
        return attendanceRepository.findByDate(date).stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<AttendanceDto> getAttendanceReport(Long employeeId, LocalDate start, LocalDate end) {
        return attendanceRepository.findByEmployeeIdAndDateBetween(employeeId, start, end).stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public long getPresentCount(LocalDate date) {
        long present = attendanceRepository.countByDateAndStatus(date, Attendance.AttendanceStatus.PRESENT);
        long late = attendanceRepository.countByDateAndStatus(date, Attendance.AttendanceStatus.LATE);
        long half = attendanceRepository.countByDateAndStatus(date, Attendance.AttendanceStatus.HALF_DAY);
        return present + late + half;
    }

    @Override
    @Transactional(readOnly = true)
    public long getAbsentCount(LocalDate date) {
        // Simple logic: total active employees - checked-in employees
        long activeCount = employeeRepository.countByStatus(Employee.EmployeeStatus.ACTIVE);
        long checkedInCount = getPresentCount(date);
        long absent = activeCount - checkedInCount;
        return absent < 0 ? 0 : absent;
    }

    @Override
    @Transactional(readOnly = true)
    public long getLateCount(LocalDate date) {
        return attendanceRepository.countByDateAndStatus(date, Attendance.AttendanceStatus.LATE);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AttendanceDto> getAllAttendance() {
        return attendanceRepository.findAll().stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    private AttendanceDto mapToDto(Attendance attendance) {
        return AttendanceDto.builder()
                .id(attendance.getId())
                .employeeId(attendance.getEmployee().getId())
                .employeeName(attendance.getEmployee().getFullName())
                .employeeCode(attendance.getEmployee().getEmployeeId())
                .date(attendance.getDate())
                .checkIn(attendance.getCheckIn())
                .checkOut(attendance.getCheckOut())
                .workingHours(attendance.getWorkingHours())
                .status(attendance.getStatus().name())
                .build();
    }
}
