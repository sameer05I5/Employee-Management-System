package com.employeesphere.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LeaveRequestDto {
    private Long id;
    private Long employeeId;
    private String employeeName;
    
    @NotBlank(message = "Leave type is required")
    private String leaveType; // SICK, CASUAL, PAID
    
    @NotNull(message = "Start date is required")
    private LocalDate startDate;
    
    @NotNull(message = "End date is required")
    private LocalDate endDate;
    
    @NotBlank(message = "Reason is required")
    private String reason;
    
    private String status; // PENDING, APPROVED, REJECTED
    
    private Long approvedById;
    private String approvedByName;
    private LocalDateTime createdAt;
}
