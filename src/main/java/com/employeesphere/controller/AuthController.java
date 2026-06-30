package com.employeesphere.controller;

import com.employeesphere.dto.AuthResponse;
import com.employeesphere.dto.LoginRequest;
import com.employeesphere.dto.SignupRequest;
import com.employeesphere.dto.EmployeeDto;
import com.employeesphere.security.JwtService;
import com.employeesphere.service.EmployeeService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.Set;
import java.util.stream.Collectors;

@Controller
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final EmployeeService employeeService;

    public AuthController(AuthenticationManager authenticationManager, JwtService jwtService, EmployeeService employeeService) {
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
        this.employeeService = employeeService;
    }

    // 1. Web View Endpoints
    @GetMapping("/login")
    public String showLoginForm(Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() && !auth.getPrincipal().equals("anonymousUser")) {
            return "redirect:/dashboard";
        }
        return "login";
    }

    @PostMapping("/login")
    public String handleWebLogin(@RequestParam String email, @RequestParam String password, 
                                 HttpServletResponse response, Model model) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(email, password)
            );
            SecurityContextHolder.getContext().setAuthentication(authentication);
            String jwt = jwtService.generateToken(email);

            // Set JWT as HttpOnly Cookie
            Cookie cookie = new Cookie("jwt_token", jwt);
            cookie.setHttpOnly(true);
            cookie.setSecure(false); // Enable secure in production
            cookie.setPath("/");
            cookie.setMaxAge(24 * 60 * 60); // 24 hours
            response.addCookie(cookie);

            return "redirect:/dashboard";
        } catch (Exception e) {
            model.addAttribute("error", "Invalid credentials. Please try again.");
            return "login";
        }
    }

    @GetMapping("/signup")
    public String showSignupForm(Model model) {
        model.addAttribute("signupRequest", new SignupRequest());
        return "signup";
    }

    @PostMapping("/signup")
    public String handleWebSignup(@ModelAttribute("signupRequest") @Valid SignupRequest signupRequest, Model model) {
        try {
            employeeService.signupEmployee(signupRequest);
            model.addAttribute("success", "Registration successful! You can now log in.");
            return "login";
        } catch (Exception e) {
            model.addAttribute("error", "Error creating employee: " + e.getMessage());
            return "signup";
        }
    }

    @GetMapping("/logout")
    public String handleLogout(HttpServletResponse response) {
        // Clear Authentication context
        SecurityContextHolder.clearContext();

        // Expire the JWT cookie
        Cookie cookie = new Cookie("jwt_token", null);
        cookie.setHttpOnly(true);
        cookie.setSecure(false);
        cookie.setPath("/");
        cookie.setMaxAge(0);
        response.addCookie(cookie);

        return "redirect:/login?logout";
    }

    // 2. REST API Endpoints for REST Clients
    @PostMapping("/api/auth/login")
    @ResponseBody
    public AuthResponse handleApiLogin(@Valid @RequestBody LoginRequest loginRequest) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginRequest.getEmail(), loginRequest.getPassword())
        );
        SecurityContextHolder.getContext().setAuthentication(authentication);
        String jwt = jwtService.generateToken(loginRequest.getEmail());

        Set<String> roles = authentication.getAuthorities().stream()
                .map(auth -> auth.getAuthority())
                .collect(Collectors.toSet());

        return AuthResponse.builder()
                .token(jwt)
                .email(loginRequest.getEmail())
                .roles(roles)
                .build();
    }

    @PostMapping("/api/auth/signup")
    @ResponseBody
    public EmployeeDto handleApiSignup(@Valid @RequestBody SignupRequest signupRequest) {
        return employeeService.signupEmployee(signupRequest);
    }
}
