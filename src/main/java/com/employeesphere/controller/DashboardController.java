package com.employeesphere.controller;

import com.employeesphere.dto.AttendanceDto;
import com.employeesphere.dto.EmployeeDto;
import com.employeesphere.entity.Attendance;
import com.employeesphere.entity.Employee;
import com.employeesphere.entity.LeaveRequest;
import com.employeesphere.entity.Notification;
import com.employeesphere.repository.AttendanceRepository;
import com.employeesphere.repository.DepartmentRepository;
import com.employeesphere.repository.EmployeeRepository;
import com.employeesphere.repository.LeaveRequestRepository;
import com.employeesphere.service.*;
import com.employeesphere.util.SecurityUtils;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Controller
public class DashboardController {

    private final EmployeeService employeeService;
    private final AttendanceService attendanceService;
    private final LeaveService leaveService;
    private final DepartmentService departmentService;
    private final NotificationService notificationService;
    private final AttendanceRepository attendanceRepository;
    private final LeaveRequestRepository leaveRequestRepository;

    public DashboardController(EmployeeService employeeService,
                               AttendanceService attendanceService,
                               LeaveService leaveService,
                               DepartmentService departmentService,
                               NotificationService notificationService,
                               AttendanceRepository attendanceRepository,
                               LeaveRequestRepository leaveRequestRepository) {
        this.employeeService = employeeService;
        this.attendanceService = attendanceService;
        this.leaveService = leaveService;
        this.departmentService = departmentService;
        this.notificationService = notificationService;
        this.attendanceRepository = attendanceRepository;
        this.leaveRequestRepository = leaveRequestRepository;
    }

    @GetMapping({"/", "/dashboard"})
    public String viewDashboard(Model model) {
        Optional<String> currentUsernameOpt = SecurityUtils.getCurrentUsername();
        if (currentUsernameOpt.isEmpty()) {
            return "redirect:/login";
        }

        String username = currentUsernameOpt.get();
        EmployeeDto employee = employeeService.getEmployeeByUsername(username);
        model.addAttribute("employee", employee);

        // Fetch User Notifications
        List<Notification> notifications = notificationService.getUnreadNotificationsForEmployee(employee.getId());
        model.addAttribute("notifications", notifications);
        model.addAttribute("unreadNotificationCount", notifications.size());

        // Fetch Today's Attendance for check-in button display
        AttendanceDto todayAttendance = attendanceService.getTodayAttendance(employee.getId());
        model.addAttribute("todayAttendance", todayAttendance);

        // Populate Role-based Statistics
        if (SecurityUtils.hasRole("ROLE_ADMIN") || SecurityUtils.hasRole("ROLE_HR")) {
            // Admin/HR statistics
            long totalEmp = employeeService.countActiveEmployees();
            long presentToday = attendanceService.getPresentCount(LocalDate.now());
            long absentToday = attendanceService.getAbsentCount(LocalDate.now());
            long lateToday = attendanceService.getLateCount(LocalDate.now());
            long pendingLeaves = leaveService.getPendingCount();

            model.addAttribute("totalEmployeesCount", totalEmp);
            model.addAttribute("presentCount", presentToday);
            model.addAttribute("absentCount", absentToday);
            model.addAttribute("lateCount", lateToday);
            model.addAttribute("pendingLeavesCount", pendingLeaves);

            // Recent Leaves
            List<LeaveRequest> recentLeaves = leaveRequestRepository.findAll().stream()
                    .sorted((a, b) -> b.getCreatedAt().compareTo(a.getCreatedAt()))
                    .limit(5)
                    .collect(Collectors.toList());
            model.addAttribute("recentLeaves", recentLeaves);

            // Recent Check-ins
            List<Attendance> recentCheckins = attendanceRepository.findByDate(LocalDate.now()).stream()
                    .sorted((a, b) -> b.getCheckIn().compareTo(a.getCheckIn()))
                    .limit(5)
                    .collect(Collectors.toList());
            model.addAttribute("recentCheckins", recentCheckins);
            
            // Department distribution for charts
            List<Map<String, Object>> deptStats = departmentService.getAllDepartments().stream()
                    .map(dept -> {
                        Map<String, Object> stat = new HashMap<>();
                        stat.put("name", dept.getName());
                        stat.put("count", dept.getEmployeeCount());
                        return stat;
                    }).collect(Collectors.toList());
            model.addAttribute("deptStats", deptStats);

        } else if (SecurityUtils.hasRole("ROLE_MANAGER")) {
            // Manager Statistics (their team members)
            List<EmployeeDto> subordinates = employeeService.getSubordinates(employee.getId());
            model.addAttribute("teamSize", subordinates.size());
            
            long pendingLeaves = leaveService.getManagerPendingRequests(employee.getId()).size();
            model.addAttribute("pendingTeamLeaves", pendingLeaves);

            // Team checkin status today
            List<Long> subIds = subordinates.stream().map(EmployeeDto::getId).toList();
            long teamPresent = attendanceRepository.findByDate(LocalDate.now()).stream()
                    .filter(a -> subIds.contains(a.getEmployee().getId()))
                    .count();
            model.addAttribute("teamPresentToday", teamPresent);
            model.addAttribute("teamAbsentToday", subordinates.size() - teamPresent);

            // Manager team members
            model.addAttribute("teamList", subordinates);
            
        }

        // Common Employee stats (shown to everyone including Manager and Regular Employee)
        long sickBalance = leaveService.getLeaveBalance(employee.getId(), "SICK");
        long casualBalance = leaveService.getLeaveBalance(employee.getId(), "CASUAL");
        long paidBalance = leaveService.getLeaveBalance(employee.getId(), "PAID");

        model.addAttribute("sickBalance", sickBalance);
        model.addAttribute("casualBalance", casualBalance);
        model.addAttribute("paidBalance", paidBalance);

        // Fetch User's Attendance History
        List<AttendanceDto> history = attendanceService.getAttendanceHistory(employee.getId()).stream()
                .limit(7)
                .collect(Collectors.toList());
        model.addAttribute("attendanceHistory", history);

        return "dashboard";
    }
}
