package com.employeesphere.controller;

import com.employeesphere.dto.EmployeeDto;
import com.employeesphere.dto.PayrollDto;
import com.employeesphere.service.EmployeeService;
import com.employeesphere.service.PayrollService;
import com.employeesphere.util.SecurityUtils;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.util.List;

@Controller
public class PayrollController {

    private final PayrollService payrollService;
    private final EmployeeService employeeService;

    public PayrollController(PayrollService payrollService, EmployeeService employeeService) {
        this.payrollService = payrollService;
        this.employeeService = employeeService;
    }

    // 1. Web View MVC Endpoints
    @GetMapping("/payroll")
    public String showPayroll(Model model) {
        String username = SecurityUtils.getCurrentUsername().orElseThrow();
        EmployeeDto employee = employeeService.getEmployeeByUsername(username);
        model.addAttribute("employee", employee);

        if (SecurityUtils.hasRole("ROLE_ADMIN") || SecurityUtils.hasRole("ROLE_HR")) {
            // Admin lists all payrolls and loaded employees for creation
            List<PayrollDto> allPayrolls = payrollService.getAllPayrolls();
            model.addAttribute("payrolls", allPayrolls);
            
            List<EmployeeDto> activeEmployees = employeeService.getAllEmployees();
            model.addAttribute("employees", activeEmployees);
            
            LocalDate today = LocalDate.now();
            model.addAttribute("currentMonth", today.getMonthValue());
            model.addAttribute("currentYear", today.getYear());
        } else {
            // Employee sees their own payroll history
            List<PayrollDto> history = payrollService.getEmployeePayrollHistory(employee.getId());
            model.addAttribute("payrolls", history);
        }

        return "payroll/list";
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'HR')")
    @PostMapping("/payroll/generate")
    public String generatePayroll(@RequestParam Long employeeId,
                                  @RequestParam Integer month,
                                  @RequestParam Integer year,
                                  @RequestParam(defaultValue = "0") Double allowances,
                                  @RequestParam(defaultValue = "0") Double deductions,
                                  RedirectAttributes redirectAttributes) {
        try {
            payrollService.generatePayroll(employeeId, month, year, allowances, deductions);
            redirectAttributes.addFlashAttribute("success", "Payroll record generated successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/payroll";
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'HR')")
    @PostMapping("/payroll/pay/{id}")
    public String processPayment(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            payrollService.updatePayrollStatus(id, "PAID");
            redirectAttributes.addFlashAttribute("success", "Salary payout completed successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/payroll";
    }

    @GetMapping("/payroll/payslip/{id}")
    public String viewPayslip(@PathVariable Long id, Model model) {
        String username = SecurityUtils.getCurrentUsername().orElseThrow();
        EmployeeDto employee = employeeService.getEmployeeByUsername(username);
        model.addAttribute("employee", employee);

        PayrollDto payslip = payrollService.getPayrollById(id);
        
        // Security check: employees can only view their own payslips
        if (!SecurityUtils.hasRole("ROLE_ADMIN") && !SecurityUtils.hasRole("ROLE_HR") && !payslip.getEmployeeId().equals(employee.getId())) {
            return "redirect:/dashboard";
        }

        model.addAttribute("payslip", payslip);
        
        // Fetch detailed employee details for the payslip layout
        EmployeeDto detail = employeeService.getEmployeeById(payslip.getEmployeeId());
        model.addAttribute("employeeDetail", detail);

        return "payroll/payslip";
    }

    // 2. REST API Controller Endpoints
    @PreAuthorize("hasAnyRole('ADMIN', 'HR')")
    @PostMapping("/api/payroll/generate")
    @ResponseBody
    public ResponseEntity<PayrollDto> apiGeneratePayroll(
            @RequestParam Long employeeId,
            @RequestParam Integer month,
            @RequestParam Integer year,
            @RequestParam(defaultValue = "0") Double allowances,
            @RequestParam(defaultValue = "0") Double deductions) {
        return ResponseEntity.ok(payrollService.generatePayroll(employeeId, month, year, allowances, deductions));
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'HR')")
    @PostMapping("/api/payroll/pay/{id}")
    @ResponseBody
    public ResponseEntity<PayrollDto> apiProcessPayment(@PathVariable Long id) {
        return ResponseEntity.ok(payrollService.updatePayrollStatus(id, "PAID"));
    }

    @GetMapping("/api/payroll/history/{employeeId}")
    @ResponseBody
    public ResponseEntity<List<PayrollDto>> apiGetHistory(@PathVariable Long employeeId) {
        return ResponseEntity.ok(payrollService.getEmployeePayrollHistory(employeeId));
    }

    @GetMapping("/api/payroll/{id}")
    @ResponseBody
    public ResponseEntity<PayrollDto> apiGetById(@PathVariable Long id) {
        return ResponseEntity.ok(payrollService.getPayrollById(id));
    }
}
