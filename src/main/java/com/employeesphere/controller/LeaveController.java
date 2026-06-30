package com.employeesphere.controller;

import com.employeesphere.dto.EmployeeDto;
import com.employeesphere.dto.LeaveRequestDto;
import com.employeesphere.service.EmployeeService;
import com.employeesphere.service.LeaveService;
import com.employeesphere.util.SecurityUtils;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
public class LeaveController {

    private final LeaveService leaveService;
    private final EmployeeService employeeService;

    public LeaveController(LeaveService leaveService, EmployeeService employeeService) {
        this.leaveService = leaveService;
        this.employeeService = employeeService;
    }

    // 1. Web View MVC Endpoints
    @GetMapping("/leaves")
    public String viewLeaves(Model model) {
        String username = SecurityUtils.getCurrentUsername().orElseThrow();
        EmployeeDto employee = employeeService.getEmployeeByUsername(username);
        model.addAttribute("employee", employee);

        List<LeaveRequestDto> history = leaveService.getEmployeeLeaveHistory(employee.getId());
        model.addAttribute("leavesList", history);

        long sick = leaveService.getLeaveBalance(employee.getId(), "SICK");
        long casual = leaveService.getLeaveBalance(employee.getId(), "CASUAL");
        long paid = leaveService.getLeaveBalance(employee.getId(), "PAID");

        model.addAttribute("sickBalance", sick);
        model.addAttribute("casualBalance", casual);
        model.addAttribute("paidBalance", paid);

        return "leaves/list";
    }

    @GetMapping("/leaves/apply")
    public String showApplyForm(Model model) {
        String username = SecurityUtils.getCurrentUsername().orElseThrow();
        EmployeeDto employee = employeeService.getEmployeeByUsername(username);
        model.addAttribute("employee", employee);

        LeaveRequestDto form = new LeaveRequestDto();
        form.setEmployeeId(employee.getId());
        model.addAttribute("leaveForm", form);

        return "leaves/apply";
    }

    @PostMapping("/leaves/apply")
    public String applyLeave(@ModelAttribute("leaveForm") @Valid LeaveRequestDto dto, 
                             RedirectAttributes redirectAttributes, Model model) {
        try {
            String username = SecurityUtils.getCurrentUsername().orElseThrow();
            EmployeeDto employee = employeeService.getEmployeeByUsername(username);
            dto.setEmployeeId(employee.getId());
            leaveService.applyLeave(dto);
            redirectAttributes.addFlashAttribute("success", "Leave request submitted successfully!");
            return "redirect:/leaves";
        } catch (Exception e) {
            String username = SecurityUtils.getCurrentUsername().orElseThrow();
            model.addAttribute("employee", employeeService.getEmployeeByUsername(username));
            model.addAttribute("error", e.getMessage());
            return "leaves/apply";
        }
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'HR', 'MANAGER')")
    @GetMapping("/leaves/pending")
    public String viewPendingRequests(Model model) {
        String username = SecurityUtils.getCurrentUsername().orElseThrow();
        EmployeeDto employee = employeeService.getEmployeeByUsername(username);
        model.addAttribute("employee", employee);

        List<LeaveRequestDto> pendingList;
        if (SecurityUtils.hasRole("ROLE_ADMIN") || SecurityUtils.hasRole("ROLE_HR")) {
            pendingList = leaveService.getPendingLeaveRequests();
        } else {
            pendingList = leaveService.getManagerPendingRequests(employee.getId());
        }

        model.addAttribute("pendingList", pendingList);
        return "leaves/pending";
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'HR', 'MANAGER')")
    @PostMapping("/leaves/approve/{id}")
    public String approveLeave(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            String username = SecurityUtils.getCurrentUsername().orElseThrow();
            EmployeeDto manager = employeeService.getEmployeeByUsername(username);
            leaveService.approveLeave(id, manager.getId());
            redirectAttributes.addFlashAttribute("success", "Leave request approved successfully.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/leaves/pending";
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'HR', 'MANAGER')")
    @PostMapping("/leaves/reject/{id}")
    public String rejectLeave(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            String username = SecurityUtils.getCurrentUsername().orElseThrow();
            EmployeeDto manager = employeeService.getEmployeeByUsername(username);
            leaveService.rejectLeave(id, manager.getId());
            redirectAttributes.addFlashAttribute("success", "Leave request rejected successfully.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/leaves/pending";
    }

    // 2. REST API Controller Endpoints
    @PostMapping("/api/leaves/apply")
    @ResponseBody
    public ResponseEntity<LeaveRequestDto> apiApplyLeave(@RequestBody @Valid LeaveRequestDto dto) {
        return ResponseEntity.ok(leaveService.applyLeave(dto));
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'HR', 'MANAGER')")
    @PostMapping("/api/leaves/approve/{id}")
    @ResponseBody
    public ResponseEntity<LeaveRequestDto> apiApproveLeave(@PathVariable Long id, @RequestParam Long approvedById) {
        return ResponseEntity.ok(leaveService.approveLeave(id, approvedById));
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'HR', 'MANAGER')")
    @PostMapping("/api/leaves/reject/{id}")
    @ResponseBody
    public ResponseEntity<LeaveRequestDto> apiRejectLeave(@PathVariable Long id, @RequestParam Long rejectedById) {
        return ResponseEntity.ok(leaveService.rejectLeave(id, rejectedById));
    }

    @GetMapping("/api/leaves/history/{employeeId}")
    @ResponseBody
    public ResponseEntity<List<LeaveRequestDto>> apiGetLeaveHistory(@PathVariable Long employeeId) {
        return ResponseEntity.ok(leaveService.getEmployeeLeaveHistory(employeeId));
    }

    @GetMapping("/api/leaves/pending")
    @ResponseBody
    public ResponseEntity<List<LeaveRequestDto>> apiGetPending() {
        return ResponseEntity.ok(leaveService.getPendingLeaveRequests());
    }
}
