package com.employeesphere.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PayrollDto {
    private Long id;
    
    @NotNull(message = "Employee is required")
    private Long employeeId;
    private String employeeName;
    private String employeeCode;
    
    @NotNull(message = "Month is required")
    @Min(1) @Max(12)
    private Integer month;
    
    @NotNull(message = "Year is required")
    private Integer year;
    
    private Double basicSalary;
    
    @Builder.Default
    private Double allowances = 0.0;
    
    @Builder.Default
    private Double deductions = 0.0;
    
    private Double netSalary;
    
    private String status; // DRAFT, PAID
    private LocalDate paidDate;
}
