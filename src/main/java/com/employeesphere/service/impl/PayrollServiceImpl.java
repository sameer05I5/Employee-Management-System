package com.employeesphere.service.impl;

import com.employeesphere.dto.PayrollDto;
import com.employeesphere.entity.Employee;
import com.employeesphere.entity.Notification;
import com.employeesphere.entity.Payroll;
import com.employeesphere.exception.BadRequestException;
import com.employeesphere.exception.ResourceNotFoundException;
import com.employeesphere.repository.EmployeeRepository;
import com.employeesphere.repository.NotificationRepository;
import com.employeesphere.repository.PayrollRepository;
import com.employeesphere.service.PayrollService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional
public class PayrollServiceImpl implements PayrollService {

    private final PayrollRepository payrollRepository;
    private final EmployeeRepository employeeRepository;
    private final NotificationRepository notificationRepository;

    public PayrollServiceImpl(PayrollRepository payrollRepository,
                              EmployeeRepository employeeRepository,
                              NotificationRepository notificationRepository) {
        this.payrollRepository = payrollRepository;
        this.employeeRepository = employeeRepository;
        this.notificationRepository = notificationRepository;
    }

    @Override
    public PayrollDto generatePayroll(Long employeeId, Integer month, Integer year, Double allowances, Double deductions) {
        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found with id: " + employeeId));

        Optional<Payroll> existing = payrollRepository.findByEmployeeIdAndMonthAndYear(employeeId, month, year);
        if (existing.isPresent()) {
            throw new BadRequestException("Payroll already generated for this employee for " + month + "/" + year);
        }

        double basicSalary = employee.getSalary() != null ? employee.getSalary() : 0.0;
        double netSalary = basicSalary + (allowances != null ? allowances : 0.0) - (deductions != null ? deductions : 0.0);

        Payroll payroll = Payroll.builder()
                .employee(employee)
                .month(month)
                .year(year)
                .basicSalary(basicSalary)
                .allowances(allowances != null ? allowances : 0.0)
                .deductions(deductions != null ? deductions : 0.0)
                .netSalary(Math.max(0.0, netSalary))
                .status(Payroll.PayrollStatus.DRAFT)
                .build();

        Payroll saved = payrollRepository.save(payroll);
        return mapToDto(saved);
    }

    @Override
    public PayrollDto updatePayrollStatus(Long id, String status) {
        Payroll payroll = payrollRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Payroll record not found with id: " + id));

        Payroll.PayrollStatus pStatus = Payroll.PayrollStatus.valueOf(status.toUpperCase());
        payroll.setStatus(pStatus);

        if (pStatus == Payroll.PayrollStatus.PAID) {
            payroll.setPaidDate(LocalDate.now());

            // Notify Employee
            Notification notification = Notification.builder()
                    .employee(payroll.getEmployee())
                    .title("Salary Disbursed")
                    .message("Your salary for the month of " + payroll.getMonth() + "/" + payroll.getYear() + " has been processed and paid. Net Amount: $" + payroll.getNetSalary())
                    .build();
            notificationRepository.save(notification);
        }

        Payroll saved = payrollRepository.save(payroll);
        return mapToDto(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public PayrollDto getPayrollById(Long id) {
        Payroll payroll = payrollRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Payroll record not found with id: " + id));
        return mapToDto(payroll);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PayrollDto> getEmployeePayrollHistory(Long employeeId) {
        return payrollRepository.findByEmployeeIdOrderByYearDescMonthDesc(employeeId).stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<PayrollDto> getPayrollsByMonthAndYear(Integer month, Integer year) {
        return payrollRepository.findByMonthAndYear(month, year).stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<PayrollDto> getAllPayrolls() {
        return payrollRepository.findAll().stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    private PayrollDto mapToDto(Payroll payroll) {
        return PayrollDto.builder()
                .id(payroll.getId())
                .employeeId(payroll.getEmployee().getId())
                .employeeName(payroll.getEmployee().getFullName())
                .employeeCode(payroll.getEmployee().getEmployeeId())
                .month(payroll.getMonth())
                .year(payroll.getYear())
                .basicSalary(payroll.getBasicSalary())
                .allowances(payroll.getAllowances())
                .deductions(payroll.getDeductions())
                .netSalary(payroll.getNetSalary())
                .status(payroll.getStatus().name())
                .paidDate(payroll.getPaidDate())
                .build();
    }
}
