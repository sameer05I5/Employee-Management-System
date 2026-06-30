package com.employeesphere.service.impl;

import com.employeesphere.dto.DepartmentDto;
import com.employeesphere.entity.Department;
import com.employeesphere.entity.Employee;
import com.employeesphere.exception.BadRequestException;
import com.employeesphere.exception.ResourceNotFoundException;
import com.employeesphere.repository.DepartmentRepository;
import com.employeesphere.repository.EmployeeRepository;
import com.employeesphere.service.DepartmentService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class DepartmentServiceImpl implements DepartmentService {

    private final DepartmentRepository departmentRepository;
    private final EmployeeRepository employeeRepository;

    public DepartmentServiceImpl(DepartmentRepository departmentRepository, EmployeeRepository employeeRepository) {
        this.departmentRepository = departmentRepository;
        this.employeeRepository = employeeRepository;
    }

    @Override
    public DepartmentDto createDepartment(DepartmentDto dto) {
        if (departmentRepository.findByCode(dto.getCode()).isPresent()) {
            throw new BadRequestException("Department code already exists: " + dto.getCode());
        }
        if (departmentRepository.findByName(dto.getName()).isPresent()) {
            throw new BadRequestException("Department name already exists: " + dto.getName());
        }

        Employee head = null;
        if (dto.getDepartmentHeadId() != null) {
            head = employeeRepository.findById(dto.getDepartmentHeadId())
                    .orElseThrow(() -> new ResourceNotFoundException("Employee for Department Head not found: " + dto.getDepartmentHeadId()));
        }

        Department department = Department.builder()
                .name(dto.getName())
                .code(dto.getCode().toUpperCase())
                .description(dto.getDescription())
                .departmentHead(head)
                .build();

        Department saved = departmentRepository.save(department);
        return mapToDto(saved);
    }

    @Override
    public DepartmentDto updateDepartment(Long id, DepartmentDto dto) {
        Department dept = departmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Department not found: " + id));

        if (!dept.getCode().equalsIgnoreCase(dto.getCode())) {
            if (departmentRepository.findByCode(dto.getCode()).isPresent()) {
                throw new BadRequestException("Department code already exists: " + dto.getCode());
            }
            dept.setCode(dto.getCode().toUpperCase());
        }

        if (!dept.getName().equalsIgnoreCase(dto.getName())) {
            if (departmentRepository.findByName(dto.getName()).isPresent()) {
                throw new BadRequestException("Department name already exists: " + dto.getName());
            }
            dept.setName(dto.getName());
        }

        dept.setDescription(dto.getDescription());

        if (dto.getDepartmentHeadId() != null) {
            Employee head = employeeRepository.findById(dto.getDepartmentHeadId())
                    .orElseThrow(() -> new ResourceNotFoundException("Employee for Department Head not found: " + dto.getDepartmentHeadId()));
            dept.setDepartmentHead(head);
        } else {
            dept.setDepartmentHead(null);
        }

        Department saved = departmentRepository.save(dept);
        return mapToDto(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public DepartmentDto getDepartmentById(Long id) {
        Department dept = departmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Department not found with id: " + id));
        return mapToDto(dept);
    }

    @Override
    @Transactional(readOnly = true)
    public DepartmentDto getDepartmentByCode(String code) {
        Department dept = departmentRepository.findByCode(code.toUpperCase())
                .orElseThrow(() -> new ResourceNotFoundException("Department not found with code: " + code));
        return mapToDto(dept);
    }

    @Override
    @Transactional(readOnly = true)
    public List<DepartmentDto> getAllDepartments() {
        return departmentRepository.findAll().stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Override
    public void deleteDepartment(Long id) {
        Department dept = departmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Department not found with id: " + id));

        // Clear department references in Employees
        List<Employee> employees = employeeRepository.findByDepartmentId(id);
        for (Employee emp : employees) {
            emp.setDepartment(null);
            employeeRepository.save(emp);
        }

        departmentRepository.delete(dept);
    }

    private DepartmentDto mapToDto(Department dept) {
        int employeeCount = employeeRepository.findByDepartmentId(dept.getId()).size();
        return DepartmentDto.builder()
                .id(dept.getId())
                .name(dept.getName())
                .code(dept.getCode())
                .description(dept.getDescription())
                .departmentHeadId(dept.getDepartmentHead() != null ? dept.getDepartmentHead().getId() : null)
                .departmentHeadName(dept.getDepartmentHead() != null ? dept.getDepartmentHead().getFullName() : "None Assigned")
                .employeeCount(employeeCount)
                .build();
    }
}
