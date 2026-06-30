package com.employeesphere.controller;

import com.employeesphere.dto.DepartmentDto;
import com.employeesphere.dto.EmployeeDto;
import com.employeesphere.service.DepartmentService;
import com.employeesphere.service.EmployeeService;
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
public class DepartmentController {

    private final DepartmentService departmentService;
    private final EmployeeService employeeService;

    public DepartmentController(DepartmentService departmentService, EmployeeService employeeService) {
        this.departmentService = departmentService;
        this.employeeService = employeeService;
    }

    // 1. Web View MVC Endpoints
    @GetMapping("/departments")
    public String listDepartments(Model model) {
        String username = SecurityUtils.getCurrentUsername().orElseThrow();
        model.addAttribute("employee", employeeService.getEmployeeByUsername(username));

        List<DepartmentDto> list = departmentService.getAllDepartments();
        model.addAttribute("departments", list);
        return "departments/list";
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'HR')")
    @GetMapping("/departments/new")
    public String showCreateForm(Model model) {
        String username = SecurityUtils.getCurrentUsername().orElseThrow();
        model.addAttribute("employee", employeeService.getEmployeeByUsername(username));

        model.addAttribute("departmentForm", new DepartmentDto());
        model.addAttribute("employees", employeeService.getAllEmployees());
        return "departments/form";
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'HR')")
    @PostMapping("/departments")
    public String saveDepartment(@ModelAttribute("departmentForm") @Valid DepartmentDto dto, 
                                 RedirectAttributes redirectAttributes, Model model) {
        try {
            departmentService.createDepartment(dto);
            redirectAttributes.addFlashAttribute("success", "Department created successfully!");
            return "redirect:/departments";
        } catch (Exception e) {
            String username = SecurityUtils.getCurrentUsername().orElseThrow();
            model.addAttribute("employee", employeeService.getEmployeeByUsername(username));
            model.addAttribute("error", e.getMessage());
            model.addAttribute("employees", employeeService.getAllEmployees());
            return "departments/form";
        }
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'HR')")
    @GetMapping("/departments/edit/{id}")
    public String showEditForm(@PathVariable Long id, Model model) {
        String username = SecurityUtils.getCurrentUsername().orElseThrow();
        model.addAttribute("employee", employeeService.getEmployeeByUsername(username));

        DepartmentDto departmentForm = departmentService.getDepartmentById(id);
        model.addAttribute("departmentForm", departmentForm);
        model.addAttribute("employees", employeeService.getAllEmployees());
        return "departments/form";
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'HR')")
    @PostMapping("/departments/edit/{id}")
    public String updateDepartment(@PathVariable Long id, 
                                   @ModelAttribute("departmentForm") @Valid DepartmentDto dto, 
                                   RedirectAttributes redirectAttributes, Model model) {
        try {
            departmentService.updateDepartment(id, dto);
            redirectAttributes.addFlashAttribute("success", "Department updated successfully!");
            return "redirect:/departments";
        } catch (Exception e) {
            String username = SecurityUtils.getCurrentUsername().orElseThrow();
            model.addAttribute("employee", employeeService.getEmployeeByUsername(username));
            model.addAttribute("error", e.getMessage());
            model.addAttribute("employees", employeeService.getAllEmployees());
            return "departments/form";
        }
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'HR')")
    @GetMapping("/departments/delete/{id}")
    public String deleteDepartment(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            departmentService.deleteDepartment(id);
            redirectAttributes.addFlashAttribute("success", "Department deleted successfully.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/departments";
    }

    // 2. REST API Controller Endpoints
    @GetMapping("/api/departments")
    @ResponseBody
    public ResponseEntity<List<DepartmentDto>> apiGetAll() {
        return ResponseEntity.ok(departmentService.getAllDepartments());
    }

    @GetMapping("/api/departments/{id}")
    @ResponseBody
    public ResponseEntity<DepartmentDto> apiGetById(@PathVariable Long id) {
        return ResponseEntity.ok(departmentService.getDepartmentById(id));
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'HR')")
    @PostMapping("/api/departments")
    @ResponseBody
    public ResponseEntity<DepartmentDto> apiCreate(@RequestBody @Valid DepartmentDto dto) {
        return ResponseEntity.ok(departmentService.createDepartment(dto));
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'HR')")
    @PutMapping("/api/departments/{id}")
    @ResponseBody
    public ResponseEntity<DepartmentDto> apiUpdate(@PathVariable Long id, @RequestBody @Valid DepartmentDto dto) {
        return ResponseEntity.ok(departmentService.updateDepartment(id, dto));
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'HR')")
    @DeleteMapping("/api/departments/{id}")
    @ResponseBody
    public ResponseEntity<Void> apiDelete(@PathVariable Long id) {
        departmentService.deleteDepartment(id);
        return ResponseEntity.noContent().build();
    }
}
