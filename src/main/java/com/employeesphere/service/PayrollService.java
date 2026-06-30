package com.employeesphere.service;

import com.employeesphere.dto.PayrollDto;

import java.util.List;

public interface PayrollService {
    PayrollDto generatePayroll(Long employeeId, Integer month, Integer year, Double allowances, Double deductions);
    PayrollDto updatePayrollStatus(Long id, String status);
    PayrollDto getPayrollById(Long id);
    List<PayrollDto> getEmployeePayrollHistory(Long employeeId);
    List<PayrollDto> getPayrollsByMonthAndYear(Integer month, Integer year);
    List<PayrollDto> getAllPayrolls();
}
