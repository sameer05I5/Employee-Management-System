package com.employeesphere.service.impl;

import com.employeesphere.dto.EmployeeDto;
import com.employeesphere.dto.SignupRequest;
import com.employeesphere.entity.Department;
import com.employeesphere.entity.Employee;
import com.employeesphere.entity.Role;
import com.employeesphere.entity.User;
import com.employeesphere.exception.BadRequestException;
import com.employeesphere.exception.ResourceNotFoundException;
import com.employeesphere.repository.DepartmentRepository;
import com.employeesphere.repository.EmployeeRepository;
import com.employeesphere.repository.RoleRepository;
import com.employeesphere.repository.UserRepository;
import com.employeesphere.service.EmployeeService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Transactional
public class EmployeeServiceImpl implements EmployeeService {

    private final EmployeeRepository employeeRepository;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final DepartmentRepository departmentRepository;
    private final PasswordEncoder passwordEncoder;

    public EmployeeServiceImpl(EmployeeRepository employeeRepository,
                               UserRepository userRepository,
                               RoleRepository roleRepository,
                               DepartmentRepository departmentRepository,
                               PasswordEncoder passwordEncoder) {
        this.employeeRepository = employeeRepository;
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.departmentRepository = departmentRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public EmployeeDto saveEmployee(EmployeeDto dto) {
        if (employeeRepository.findByEmail(dto.getEmail()).isPresent()) {
            throw new BadRequestException("Email already exists: " + dto.getEmail());
        }

        // Create User entity
        Set<Role> roles = new HashSet<>();
        if (dto.getRole() != null) {
            Role.RoleType rType = Role.RoleType.valueOf(dto.getRole().toUpperCase());
            Role role = roleRepository.findByName(rType)
                    .orElseThrow(() -> new ResourceNotFoundException("Role not found: " + dto.getRole()));
            roles.add(role);
        } else {
            Role defaultRole = roleRepository.findByName(Role.RoleType.ROLE_EMPLOYEE)
                    .orElseThrow(() -> new ResourceNotFoundException("Default employee role not found"));
            roles.add(defaultRole);
        }

        User user = User.builder()
                .username(dto.getEmail())
                .password(passwordEncoder.encode("Welcome@123")) // Default password
                .enabled(true)
                .roles(roles)
                .build();

        user = userRepository.save(user);

        // Find Department and Manager
        Department dept = null;
        if (dto.getDepartmentId() != null) {
            dept = departmentRepository.findById(dto.getDepartmentId())
                    .orElseThrow(() -> new ResourceNotFoundException("Department not found: " + dto.getDepartmentId()));
        }

        Employee manager = null;
        if (dto.getManagerId() != null) {
            manager = employeeRepository.findById(dto.getManagerId())
                    .orElseThrow(() -> new ResourceNotFoundException("Manager not found: " + dto.getManagerId()));
        }

        // Generate Employee ID
        String employeeIdStr = dto.getEmployeeId();
        if (employeeIdStr == null || employeeIdStr.trim().isEmpty()) {
            employeeIdStr = "EMP-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        }

        Employee employee = Employee.builder()
                .employeeId(employeeIdStr)
                .fullName(dto.getFullName())
                .email(dto.getEmail())
                .phone(dto.getPhone())
                .designation(dto.getDesignation())
                .salary(dto.getSalary())
                .joiningDate(dto.getJoiningDate() != null ? dto.getJoiningDate() : LocalDate.now())
                .status(dto.getStatus() != null ? Employee.EmployeeStatus.valueOf(dto.getStatus()) : Employee.EmployeeStatus.ACTIVE)
                .address(dto.getAddress())
                .emergencyContact(dto.getEmergencyContact())
                .department(dept)
                .manager(manager)
                .user(user)
                .build();

        Employee saved = employeeRepository.save(employee);
        return mapToDto(saved);
    }

    @Override
    public EmployeeDto signupEmployee(SignupRequest request) {
        if (userRepository.existsByUsername(request.getEmail())) {
            throw new BadRequestException("Email already registered: " + request.getEmail());
        }

        // Create User
        Set<Role> roles = new HashSet<>();
        if (request.getRole() != null) {
            String roleName = request.getRole().startsWith("ROLE_") ? request.getRole() : "ROLE_" + request.getRole();
            Role role = roleRepository.findByName(Role.RoleType.valueOf(roleName.toUpperCase()))
                    .orElseThrow(() -> new ResourceNotFoundException("Role not found: " + request.getRole()));
            roles.add(role);
        } else {
            Role defaultRole = roleRepository.findByName(Role.RoleType.ROLE_EMPLOYEE)
                    .orElseThrow(() -> new ResourceNotFoundException("Default employee role not found"));
            roles.add(defaultRole);
        }

        User user = User.builder()
                .username(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .enabled(true)
                .roles(roles)
                .build();

        user = userRepository.save(user);

        // Find Department and Manager
        Department dept = null;
        if (request.getDepartmentId() != null) {
            dept = departmentRepository.findById(request.getDepartmentId()).orElse(null);
        }

        Employee manager = null;
        if (request.getManagerId() != null) {
            manager = employeeRepository.findById(request.getManagerId()).orElse(null);
        }

        String employeeIdStr = "EMP-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();

        Employee employee = Employee.builder()
                .employeeId(employeeIdStr)
                .fullName(request.getFullName())
                .email(request.getEmail())
                .phone(request.getPhone())
                .designation(request.getDesignation())
                .salary(request.getSalary())
                .joiningDate(LocalDate.now())
                .status(Employee.EmployeeStatus.ACTIVE)
                .address(request.getAddress())
                .emergencyContact(request.getEmergencyContact())
                .department(dept)
                .manager(manager)
                .user(user)
                .build();

        Employee saved = employeeRepository.save(employee);
        return mapToDto(saved);
    }

    @Override
    public EmployeeDto updateEmployee(Long id, EmployeeDto dto) {
        Employee employee = employeeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found: " + id));

        // Update User info if email changed
        if (!employee.getEmail().equalsIgnoreCase(dto.getEmail())) {
            if (employeeRepository.findByEmail(dto.getEmail()).isPresent()) {
                throw new BadRequestException("Email already exists: " + dto.getEmail());
            }
            User user = employee.getUser();
            user.setUsername(dto.getEmail());
            userRepository.save(user);
            employee.setEmail(dto.getEmail());
        }

        // Update Role
        if (dto.getRole() != null) {
            String roleName = dto.getRole().startsWith("ROLE_") ? dto.getRole() : "ROLE_" + dto.getRole();
            Role role = roleRepository.findByName(Role.RoleType.valueOf(roleName.toUpperCase()))
                    .orElseThrow(() -> new ResourceNotFoundException("Role not found: " + dto.getRole()));
            User user = employee.getUser();
            user.getRoles().clear();
            user.getRoles().add(role);
            userRepository.save(user);
        }

        // Update other fields
        employee.setFullName(dto.getFullName());
        employee.setPhone(dto.getPhone());
        employee.setDesignation(dto.getDesignation());
        employee.setSalary(dto.getSalary());
        if (dto.getJoiningDate() != null) {
            employee.setJoiningDate(dto.getJoiningDate());
        }
        if (dto.getStatus() != null) {
            employee.setStatus(Employee.EmployeeStatus.valueOf(dto.getStatus().toUpperCase()));
        }
        employee.setAddress(dto.getAddress());
        employee.setEmergencyContact(dto.getEmergencyContact());

        if (dto.getDepartmentId() != null) {
            Department dept = departmentRepository.findById(dto.getDepartmentId())
                    .orElseThrow(() -> new ResourceNotFoundException("Department not found: " + dto.getDepartmentId()));
            employee.setDepartment(dept);
        } else {
            employee.setDepartment(null);
        }

        if (dto.getManagerId() != null) {
            if (dto.getManagerId().equals(id)) {
                throw new BadRequestException("An employee cannot be their own manager");
            }
            Employee manager = employeeRepository.findById(dto.getManagerId())
                    .orElseThrow(() -> new ResourceNotFoundException("Manager not found: " + dto.getManagerId()));
            employee.setManager(manager);
        } else {
            employee.setManager(null);
        }

        Employee updated = employeeRepository.save(employee);
        return mapToDto(updated);
    }

    @Override
    @Transactional(readOnly = true)
    public EmployeeDto getEmployeeById(Long id) {
        Employee employee = employeeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found with id: " + id));
        return mapToDto(employee);
    }

    @Override
    @Transactional(readOnly = true)
    public EmployeeDto getEmployeeByUsername(String username) {
        Employee employee = employeeRepository.findByUserUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found with username: " + username));
        return mapToDto(employee);
    }

    @Override
    @Transactional(readOnly = true)
    public EmployeeDto getEmployeeByCode(String employeeId) {
        Employee employee = employeeRepository.findByEmployeeId(employeeId)
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found with code: " + employeeId));
        return mapToDto(employee);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<EmployeeDto> searchEmployees(String query, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Employee> employees;
        if (query == null || query.trim().isEmpty()) {
            employees = employeeRepository.findAll(pageable);
        } else {
            employees = employeeRepository.findByFullNameContainingIgnoreCaseOrEmailContainingIgnoreCaseOrEmployeeIdContainingIgnoreCase(
                    query, query, query, pageable);
        }
        return employees.map(this::mapToDto);
    }

    @Override
    @Transactional(readOnly = true)
    public List<EmployeeDto> getAllEmployees() {
        return employeeRepository.findAll().stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Override
    public void deleteEmployee(Long id) {
        Employee employee = employeeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found with id: " + id));
        
        // Remove from manager references
        List<Employee> subordinates = employeeRepository.findByManagerId(id);
        for (Employee sub : subordinates) {
            sub.setManager(null);
            employeeRepository.save(sub);
        }
        
        // Remove from department head references
        // We will do this in department service or check manually
        
        employeeRepository.delete(employee);
    }

    @Override
    @Transactional(readOnly = true)
    public List<EmployeeDto> getSubordinates(Long managerId) {
        return employeeRepository.findByManagerId(managerId).stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<EmployeeDto> getEmployeesByDepartment(Long departmentId) {
        return employeeRepository.findByDepartmentId(departmentId).stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public long countActiveEmployees() {
        return employeeRepository.countByStatus(Employee.EmployeeStatus.ACTIVE);
    }

    private EmployeeDto mapToDto(Employee employee) {
        String roleStr = employee.getUser().getRoles().isEmpty() ? "ROLE_EMPLOYEE" :
                employee.getUser().getRoles().iterator().next().getName().name();

        return EmployeeDto.builder()
                .id(employee.getId())
                .employeeId(employee.getEmployeeId())
                .fullName(employee.getFullName())
                .email(employee.getEmail())
                .phone(employee.getPhone())
                .designation(employee.getDesignation())
                .salary(employee.getSalary())
                .joiningDate(employee.getJoiningDate())
                .status(employee.getStatus().name())
                .address(employee.getAddress())
                .emergencyContact(employee.getEmergencyContact())
                .departmentId(employee.getDepartment() != null ? employee.getDepartment().getId() : null)
                .departmentName(employee.getDepartment() != null ? employee.getDepartment().getName() : "Unassigned")
                .managerId(employee.getManager() != null ? employee.getManager().getId() : null)
                .managerName(employee.getManager() != null ? employee.getManager().getFullName() : "None")
                .role(roleStr)
                .build();
    }
}
