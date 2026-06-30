package com.employeesphere.service;

import com.employeesphere.dto.DepartmentDto;

import java.util.List;

public interface DepartmentService {
    DepartmentDto createDepartment(DepartmentDto departmentDto);
    DepartmentDto updateDepartment(Long id, DepartmentDto departmentDto);
    DepartmentDto getDepartmentById(Long id);
    DepartmentDto getDepartmentByCode(String code);
    List<DepartmentDto> getAllDepartments();
    void deleteDepartment(Long id);
}
