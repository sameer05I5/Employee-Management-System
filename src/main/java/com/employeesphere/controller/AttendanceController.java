package com.employeesphere.controller;

import com.employeesphere.dto.AttendanceDto;
import com.employeesphere.dto.EmployeeDto;
import com.employeesphere.service.AttendanceService;
import com.employeesphere.service.EmployeeService;
import com.employeesphere.util.SecurityUtils;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.util.List;

@Controller
public class AttendanceController {

    private final AttendanceService attendanceService;
    private final EmployeeService employeeService;

    public AttendanceController(AttendanceService attendanceService, EmployeeService employeeService) {
        this.attendanceService = attendanceService;
        this.employeeService = employeeService;
    }

    // 1. Web View MVC Endpoints
    @GetMapping("/attendance/history")
    public String showHistory(Model model) {
        String username = SecurityUtils.getCurrentUsername().orElseThrow();
        EmployeeDto employee = employeeService.getEmployeeByUsername(username);
        model.addAttribute("employee", employee);

        List<AttendanceDto> history = attendanceService.getAttendanceHistory(employee.getId());
        model.addAttribute("attendanceHistory", history);
        return "attendance/history";
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'HR')")
    @GetMapping("/attendance/admin-logs")
    public String showAdminLogs(Model model) {
        String username = SecurityUtils.getCurrentUsername().orElseThrow();
        model.addAttribute("employee", employeeService.getEmployeeByUsername(username));

        List<AttendanceDto> allAttendance = attendanceService.getAllAttendance();
        model.addAttribute("allAttendance", allAttendance);
        return "attendance/admin_logs";
    }

    @PostMapping("/attendance/check-in")
    public String checkIn(RedirectAttributes redirectAttributes) {
        try {
            String username = SecurityUtils.getCurrentUsername().orElseThrow();
            EmployeeDto employee = employeeService.getEmployeeByUsername(username);
            attendanceService.checkIn(employee.getId());
            redirectAttributes.addFlashAttribute("success", "Checked in successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/dashboard";
    }

    @PostMapping("/attendance/check-out")
    public String checkOut(RedirectAttributes redirectAttributes) {
        try {
            String username = SecurityUtils.getCurrentUsername().orElseThrow();
            EmployeeDto employee = employeeService.getEmployeeByUsername(username);
            attendanceService.checkOut(employee.getId());
            redirectAttributes.addFlashAttribute("success", "Checked out successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/dashboard";
    }

    // 2. REST API Controller Endpoints
    @PostMapping("/api/attendance/check-in")
    @ResponseBody
    public ResponseEntity<AttendanceDto> apiCheckIn(@RequestParam Long employeeId) {
        return ResponseEntity.ok(attendanceService.checkIn(employeeId));
    }

    @PostMapping("/api/attendance/check-out")
    @ResponseBody
    public ResponseEntity<AttendanceDto> apiCheckOut(@RequestParam Long employeeId) {
        return ResponseEntity.ok(attendanceService.checkOut(employeeId));
    }

    @GetMapping("/api/attendance/history/{employeeId}")
    @ResponseBody
    public ResponseEntity<List<AttendanceDto>> apiGetHistory(@PathVariable Long employeeId) {
        return ResponseEntity.ok(attendanceService.getAttendanceHistory(employeeId));
    }

    @GetMapping("/api/attendance/today/{employeeId}")
    @ResponseBody
    public ResponseEntity<AttendanceDto> apiGetToday(@PathVariable Long employeeId) {
        return ResponseEntity.ok(attendanceService.getTodayAttendance(employeeId));
    }

    @GetMapping("/api/attendance/date")
    @ResponseBody
    public ResponseEntity<List<AttendanceDto>> apiGetByDate(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return ResponseEntity.ok(attendanceService.getAttendanceByDate(date));
    }
}
