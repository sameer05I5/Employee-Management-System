package com.employeesphere.service;

import com.employeesphere.dto.LeaveRequestDto;

import java.util.List;

public interface LeaveService {
    LeaveRequestDto applyLeave(LeaveRequestDto leaveRequestDto);
    LeaveRequestDto approveLeave(Long id, Long approvedById);
    LeaveRequestDto rejectLeave(Long id, Long rejectedById);
    List<LeaveRequestDto> getEmployeeLeaveHistory(Long employeeId);
    List<LeaveRequestDto> getPendingLeaveRequests();
    List<LeaveRequestDto> getManagerPendingRequests(Long managerId);
    long getPendingCount();
    long getLeaveBalance(Long employeeId, String leaveType);
    List<LeaveRequestDto> getAllLeaves();
}
