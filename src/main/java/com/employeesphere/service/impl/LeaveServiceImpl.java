package com.employeesphere.service.impl;

import com.employeesphere.dto.LeaveRequestDto;
import com.employeesphere.entity.Employee;
import com.employeesphere.entity.LeaveRequest;
import com.employeesphere.entity.Notification;
import com.employeesphere.exception.BadRequestException;
import com.employeesphere.exception.ResourceNotFoundException;
import com.employeesphere.repository.EmployeeRepository;
import com.employeesphere.repository.LeaveRequestRepository;
import com.employeesphere.repository.NotificationRepository;
import com.employeesphere.service.LeaveService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class LeaveServiceImpl implements LeaveService {

    private final LeaveRequestRepository leaveRequestRepository;
    private final EmployeeRepository employeeRepository;
    private final NotificationRepository notificationRepository;

    private static final int SICK_LIMIT = 12;
    private static final int CASUAL_LIMIT = 10;
    private static final int PAID_LIMIT = 15;

    public LeaveServiceImpl(LeaveRequestRepository leaveRequestRepository,
                            EmployeeRepository employeeRepository,
                            NotificationRepository notificationRepository) {
        this.leaveRequestRepository = leaveRequestRepository;
        this.employeeRepository = employeeRepository;
        this.notificationRepository = notificationRepository;
    }

    @Override
    public LeaveRequestDto applyLeave(LeaveRequestDto dto) {
        Employee employee = employeeRepository.findById(dto.getEmployeeId())
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found: " + dto.getEmployeeId()));

        if (dto.getStartDate().isAfter(dto.getEndDate())) {
            throw new BadRequestException("Start date must be before or equal to end date");
        }

        long requestedDays = ChronoUnit.DAYS.between(dto.getStartDate(), dto.getEndDate()) + 1;
        long balance = getLeaveBalance(dto.getEmployeeId(), dto.getLeaveType());

        if (requestedDays > balance) {
            throw new BadRequestException("Insufficient leave balance. Requested: " + requestedDays + " days, Available: " + balance + " days");
        }

        LeaveRequest request = LeaveRequest.builder()
                .employee(employee)
                .leaveType(LeaveRequest.LeaveType.valueOf(dto.getLeaveType().toUpperCase()))
                .startDate(dto.getStartDate())
                .endDate(dto.getEndDate())
                .reason(dto.getReason())
                .status(LeaveRequest.LeaveStatus.PENDING)
                .build();

        LeaveRequest saved = leaveRequestRepository.save(request);

        // Notify Manager
        if (employee.getManager() != null) {
            Notification notification = Notification.builder()
                    .employee(employee.getManager())
                    .title("New Leave Request")
                    .message("Employee " + employee.getFullName() + " has requested " + requestedDays + " days of " + dto.getLeaveType() + " leave.")
                    .build();
            notificationRepository.save(notification);
        }

        return mapToDto(saved);
    }

    @Override
    public LeaveRequestDto approveLeave(Long id, Long approvedById) {
        LeaveRequest request = leaveRequestRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Leave request not found: " + id));

        if (request.getStatus() != LeaveRequest.LeaveStatus.PENDING) {
            throw new BadRequestException("Leave request is already " + request.getStatus());
        }

        Employee approver = employeeRepository.findById(approvedById)
                .orElseThrow(() -> new ResourceNotFoundException("Approver not found: " + approvedById));

        request.setStatus(LeaveRequest.LeaveStatus.APPROVED);
        request.setApprovedBy(approver);
        LeaveRequest saved = leaveRequestRepository.save(request);

        // Notify Employee
        Notification notification = Notification.builder()
                .employee(request.getEmployee())
                .title("Leave Request Approved")
                .message("Your leave request from " + request.getStartDate() + " to " + request.getEndDate() + " has been approved by " + approver.getFullName() + ".")
                .build();
        notificationRepository.save(notification);

        return mapToDto(saved);
    }

    @Override
    public LeaveRequestDto rejectLeave(Long id, Long rejectedById) {
        LeaveRequest request = leaveRequestRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Leave request not found: " + id));

        if (request.getStatus() != LeaveRequest.LeaveStatus.PENDING) {
            throw new BadRequestException("Leave request is already " + request.getStatus());
        }

        Employee rejecter = employeeRepository.findById(rejectedById)
                .orElseThrow(() -> new ResourceNotFoundException("Rejecter not found: " + rejectedById));

        request.setStatus(LeaveRequest.LeaveStatus.REJECTED);
        request.setApprovedBy(rejecter);
        LeaveRequest saved = leaveRequestRepository.save(request);

        // Notify Employee
        Notification notification = Notification.builder()
                .employee(request.getEmployee())
                .title("Leave Request Rejected")
                .message("Your leave request from " + request.getStartDate() + " to " + request.getEndDate() + " has been rejected by " + rejecter.getFullName() + ".")
                .build();
        notificationRepository.save(notification);

        return mapToDto(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<LeaveRequestDto> getEmployeeLeaveHistory(Long employeeId) {
        return leaveRequestRepository.findByEmployeeIdOrderByCreatedAtDesc(employeeId).stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<LeaveRequestDto> getPendingLeaveRequests() {
        return leaveRequestRepository.findByStatus(LeaveRequest.LeaveStatus.PENDING).stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<LeaveRequestDto> getManagerPendingRequests(Long managerId) {
        return leaveRequestRepository.findByEmployeeManagerIdOrderByCreatedAtDesc(managerId).stream()
                .filter(req -> req.getStatus() == LeaveRequest.LeaveStatus.PENDING)
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public long getPendingCount() {
        return leaveRequestRepository.countByStatus(LeaveRequest.LeaveStatus.PENDING);
    }

    @Override
    @Transactional(readOnly = true)
    public long getLeaveBalance(Long employeeId, String leaveType) {
        LeaveRequest.LeaveType type = LeaveRequest.LeaveType.valueOf(leaveType.toUpperCase());
        int limit = switch (type) {
            case SICK -> SICK_LIMIT;
            case CASUAL -> CASUAL_LIMIT;
            case PAID -> PAID_LIMIT;
        };

        int currentYear = LocalDate.now().getYear();

        List<LeaveRequest> approvedLeaves = leaveRequestRepository.findByEmployeeIdOrderByCreatedAtDesc(employeeId).stream()
                .filter(req -> req.getStatus() == LeaveRequest.LeaveStatus.APPROVED
                        && req.getLeaveType() == type
                        && req.getStartDate().getYear() == currentYear)
                .toList();

        long usedDays = 0;
        for (LeaveRequest req : approvedLeaves) {
            long days = ChronoUnit.DAYS.between(req.getStartDate(), req.getEndDate()) + 1;
            usedDays += days;
        }

        return Math.max(0, limit - usedDays);
    }

    @Override
    @Transactional(readOnly = true)
    public List<LeaveRequestDto> getAllLeaves() {
        return leaveRequestRepository.findAll().stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    private LeaveRequestDto mapToDto(LeaveRequest req) {
        return LeaveRequestDto.builder()
                .id(req.getId())
                .employeeId(req.getEmployee().getId())
                .employeeName(req.getEmployee().getFullName())
                .leaveType(req.getLeaveType().name())
                .startDate(req.getStartDate())
                .endDate(req.getEndDate())
                .reason(req.getReason())
                .status(req.getStatus().name())
                .approvedById(req.getApprovedBy() != null ? req.getApprovedBy().getId() : null)
                .approvedByName(req.getApprovedBy() != null ? req.getApprovedBy().getFullName() : "Pending")
                .createdAt(req.getCreatedAt())
                .build();
    }
}
