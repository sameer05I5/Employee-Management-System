package com.employeesphere.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DepartmentDto {
    private Long id;
    
    @NotBlank(message = "Department Name is required")
    private String name;
    
    @NotBlank(message = "Department Code is required")
    private String code;
    
    private String description;
    
    private Long departmentHeadId;
    private String departmentHeadName;
    
    private Integer employeeCount;
}
