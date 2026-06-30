package com.employeesphere.service;

import com.employeesphere.dto.EmployeeDto;
import com.employeesphere.dto.SignupRequest;
import org.springframework.data.domain.Page;

import java.util.List;

public interface EmployeeService {
    EmployeeDto saveEmployee(EmployeeDto employeeDto);
    EmployeeDto updateEmployee(Long id, EmployeeDto employeeDto);
    EmployeeDto getEmployeeById(Long id);
    EmployeeDto getEmployeeByUsername(String username);
    EmployeeDto getEmployeeByCode(String employeeId);
    Page<EmployeeDto> searchEmployees(String query, int page, int size);
    List<EmployeeDto> getAllEmployees();
    void deleteEmployee(Long id);
    List<EmployeeDto> getSubordinates(Long managerId);
    List<EmployeeDto> getEmployeesByDepartment(Long departmentId);
    long countActiveEmployees();
    EmployeeDto signupEmployee(SignupRequest request);
}
