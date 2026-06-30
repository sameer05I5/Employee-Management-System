package com.employeesphere.controller;

import com.employeesphere.dto.DepartmentDto;
import com.employeesphere.dto.EmployeeDto;
import com.employeesphere.entity.Role;
import com.employeesphere.service.DepartmentService;
import com.employeesphere.service.EmployeeService;
import com.employeesphere.util.SecurityUtils;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
public class EmployeeController {

    private final EmployeeService employeeService;
    private final DepartmentService departmentService;

    public EmployeeController(EmployeeService employeeService, DepartmentService departmentService) {
        this.employeeService = employeeService;
        this.departmentService = departmentService;
    }

    // 1. Web View MVC Endpoints
    @GetMapping("/employees")
    public String listEmployees(@RequestParam(required = false) String search,
                                @RequestParam(defaultValue = "0") int page,
                                @RequestParam(defaultValue = "10") int size,
                                Model model) {
        // Load logged-in employee layout context
        String username = SecurityUtils.getCurrentUsername().orElseThrow();
        model.addAttribute("employee", employeeService.getEmployeeByUsername(username));

        Page<EmployeeDto> employeePage = employeeService.searchEmployees(search, page, size);
        model.addAttribute("employeePage", employeePage);
        model.addAttribute("searchQuery", search);
        model.addAttribute("currentPage", page);
        return "employees/list";
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'HR')")
    @GetMapping("/employees/new")
    public String showCreateForm(Model model) {
        String username = SecurityUtils.getCurrentUsername().orElseThrow();
        model.addAttribute("employee", employeeService.getEmployeeByUsername(username));

        model.addAttribute("employeeForm", new EmployeeDto());
        
        List<DepartmentDto> departments = departmentService.getAllDepartments();
        model.addAttribute("departments", departments);

        List<EmployeeDto> managers = employeeService.getAllEmployees();
        model.addAttribute("managers", managers);

        model.addAttribute("roles", Role.RoleType.values());
        return "employees/form";
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'HR')")
    @PostMapping("/employees")
    public String saveEmployee(@ModelAttribute("employeeForm") @Valid EmployeeDto dto, Model model) {
        try {
            employeeService.saveEmployee(dto);
            return "redirect:/employees";
        } catch (Exception e) {
            String username = SecurityUtils.getCurrentUsername().orElseThrow();
            model.addAttribute("employee", employeeService.getEmployeeByUsername(username));
            model.addAttribute("error", e.getMessage());
            model.addAttribute("departments", departmentService.getAllDepartments());
            model.addAttribute("managers", employeeService.getAllEmployees());
            model.addAttribute("roles", Role.RoleType.values());
            return "employees/form";
        }
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'HR')")
    @GetMapping("/employees/edit/{id}")
    public String showEditForm(@PathVariable Long id, Model model) {
        String username = SecurityUtils.getCurrentUsername().orElseThrow();
        model.addAttribute("employee", employeeService.getEmployeeByUsername(username));

        EmployeeDto employeeForm = employeeService.getEmployeeById(id);
        model.addAttribute("employeeForm", employeeForm);

        List<DepartmentDto> departments = departmentService.getAllDepartments();
        model.addAttribute("departments", departments);

        List<EmployeeDto> managers = employeeService.getAllEmployees();
        model.addAttribute("managers", managers);

        model.addAttribute("roles", Role.RoleType.values());
        return "employees/form";
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'HR')")
    @PostMapping("/employees/edit/{id}")
    public String updateEmployee(@PathVariable Long id, @ModelAttribute("employeeForm") @Valid EmployeeDto dto, Model model) {
        try {
            employeeService.updateEmployee(id, dto);
            return "redirect:/employees";
        } catch (Exception e) {
            String username = SecurityUtils.getCurrentUsername().orElseThrow();
            model.addAttribute("employee", employeeService.getEmployeeByUsername(username));
            model.addAttribute("error", e.getMessage());
            model.addAttribute("departments", departmentService.getAllDepartments());
            model.addAttribute("managers", employeeService.getAllEmployees());
            model.addAttribute("roles", Role.RoleType.values());
            return "employees/form";
        }
    }

    @GetMapping("/employees/view/{id}")
    public String viewProfile(@PathVariable Long id, Model model) {
        String username = SecurityUtils.getCurrentUsername().orElseThrow();
        EmployeeDto currentUser = employeeService.getEmployeeByUsername(username);
        model.addAttribute("employee", currentUser);

        // Security check: regular employees can only view their own profile
        if (!SecurityUtils.hasRole("ROLE_ADMIN") && !SecurityUtils.hasRole("ROLE_HR") && !SecurityUtils.hasRole("ROLE_MANAGER") && !currentUser.getId().equals(id)) {
            return "redirect:/dashboard";
        }

        EmployeeDto targetProfile = employeeService.getEmployeeById(id);
        model.addAttribute("profile", targetProfile);
        return "profile";
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'HR')")
    @GetMapping("/employees/delete/{id}")
    public String deleteEmployee(@PathVariable Long id) {
        employeeService.deleteEmployee(id);
        return "redirect:/employees";
    }

    // 2. REST API Controller Endpoints
    @GetMapping("/api/employees")
    @ResponseBody
    public ResponseEntity<Page<EmployeeDto>> apiSearchEmployees(@RequestParam(required = false) String search,
                                                                @RequestParam(defaultValue = "0") int page,
                                                                @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(employeeService.searchEmployees(search, page, size));
    }

    @GetMapping("/api/employees/{id}")
    @ResponseBody
    public ResponseEntity<EmployeeDto> apiGetEmployee(@PathVariable Long id) {
        return ResponseEntity.ok(employeeService.getEmployeeById(id));
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'HR')")
    @PostMapping("/api/employees")
    @ResponseBody
    public ResponseEntity<EmployeeDto> apiSaveEmployee(@RequestBody @Valid EmployeeDto dto) {
        return ResponseEntity.ok(employeeService.saveEmployee(dto));
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'HR')")
    @PutMapping("/api/employees/{id}")
    @ResponseBody
    public ResponseEntity<EmployeeDto> apiUpdateEmployee(@PathVariable Long id, @RequestBody @Valid EmployeeDto dto) {
        return ResponseEntity.ok(employeeService.updateEmployee(id, dto));
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'HR')")
    @DeleteMapping("/api/employees/{id}")
    @ResponseBody
    public ResponseEntity<Void> apiDeleteEmployee(@PathVariable Long id) {
        employeeService.deleteEmployee(id);
        return ResponseEntity.noContent().build();
    }
}
