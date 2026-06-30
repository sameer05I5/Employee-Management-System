package com.employeesphere.dto;

import jakarta.validation.constraints.*;
import lombok.*;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EmployeeDto {

    private Long id;
    
    private String employeeId;

    @NotBlank(message = "Full Name is required")
    private String fullName;

    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    private String email;

    private String phone;

    private String designation;

    @PositiveOrZero(message = "Salary must be zero or positive")
    private Double salary;

    private LocalDate joiningDate;

    private String status; // ACTIVE, INACTIVE, SUSPENDED

    private String address;

    private String emergencyContact;

    private Long departmentId;
    
    private String departmentName;

    private Long managerId;
    
    private String managerName;
    
    private String role; // ROLE_ADMIN, ROLE_HR, ROLE_MANAGER, ROLE_EMPLOYEE
}
