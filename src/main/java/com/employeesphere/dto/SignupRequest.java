package com.employeesphere.dto;

import jakarta.validation.constraints.*;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SignupRequest {

    @NotBlank(message = "Full Name is required")
    @Size(min = 2, max = 100, message = "Name must be between 2 and 100 characters")
    private String fullName;

    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    private String email;

    @NotBlank(message = "Password is required")
    @Size(min = 6, message = "Password must be at least 6 characters")
    private String password;

    private String phone;
    
    private String designation;
    
    private Double salary;
    
    private String address;
    
    private String emergencyContact;
    
    private String role; // ADMIN, HR, MANAGER, EMPLOYEE
    
    private Long departmentId;
    
    private Long managerId;
}
