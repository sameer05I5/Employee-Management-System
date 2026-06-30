package com.employeesphere.config;

import com.employeesphere.entity.*;
import com.employeesphere.repository.*;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Component
public class DatabaseInitializer implements CommandLineRunner {

    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    private final EmployeeRepository employeeRepository;
    private final DepartmentRepository departmentRepository;
    private final AttendanceRepository attendanceRepository;
    private final LeaveRequestRepository leaveRequestRepository;
    private final PasswordEncoder passwordEncoder;

    public DatabaseInitializer(RoleRepository roleRepository,
                               UserRepository userRepository,
                               EmployeeRepository employeeRepository,
                               DepartmentRepository departmentRepository,
                               AttendanceRepository attendanceRepository,
                               LeaveRequestRepository leaveRequestRepository,
                               PasswordEncoder passwordEncoder) {
        this.roleRepository = roleRepository;
        this.userRepository = userRepository;
        this.employeeRepository = employeeRepository;
        this.departmentRepository = departmentRepository;
        this.attendanceRepository = attendanceRepository;
        this.leaveRequestRepository = leaveRequestRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) throws Exception {
        // 1. Initialize Roles
        Role adminRole = getOrCreateRole(Role.RoleType.ROLE_ADMIN);
        Role hrRole = getOrCreateRole(Role.RoleType.ROLE_HR);
        Role managerRole = getOrCreateRole(Role.RoleType.ROLE_MANAGER);
        Role employeeRole = getOrCreateRole(Role.RoleType.ROLE_EMPLOYEE);

        if (userRepository.count() > 0) {
            // Database is already seeded
            return;
        }

        // 2. Initialize Departments
        Department engineering = Department.builder()
                .name("Engineering")
                .code("ENG")
                .description("Software engineering and development division")
                .build();
        Department hrDept = Department.builder()
                .name("Human Resources")
                .code("HR")
                .description("Talent acquisition and employee relations")
                .build();
        Department salesDept = Department.builder()
                .name("Sales")
                .code("SLS")
                .description("Business development and revenue generation")
                .build();

        engineering = departmentRepository.save(engineering);
        hrDept = departmentRepository.save(hrDept);
        salesDept = departmentRepository.save(salesDept);

        // 3. Initialize Admin Account
        User adminUser = User.builder()
                .username("admin@employeesphere.com")
                .password(passwordEncoder.encode("admin123"))
                .roles(Set.of(adminRole))
                .build();
        adminUser = userRepository.save(adminUser);

        Employee adminEmployee = Employee.builder()
                .employeeId("EMP-001")
                .fullName("Alexander Wright")
                .email("admin@employeesphere.com")
                .phone("+1 555-0100")
                .designation("VP of Engineering / Admin")
                .salary(150000.0)
                .joiningDate(LocalDate.of(2022, 1, 15))
                .status(Employee.EmployeeStatus.ACTIVE)
                .address("100 Innovation Way, Silicon Valley, CA")
                .emergencyContact("Sarah Wright: +1 555-0101")
                .user(adminUser)
                .department(engineering)
                .build();
        adminEmployee = employeeRepository.save(adminEmployee);

        // Set Admin as head of Engineering
        engineering.setDepartmentHead(adminEmployee);
        departmentRepository.save(engineering);

        // 4. Initialize HR Manager Account
        User hrUser = User.builder()
                .username("hr@employeesphere.com")
                .password(passwordEncoder.encode("hr123"))
                .roles(Set.of(hrRole))
                .build();
        hrUser = userRepository.save(hrUser);

        Employee hrEmployee = Employee.builder()
                .employeeId("EMP-002")
                .fullName("Sophia Martinez")
                .email("hr@employeesphere.com")
                .phone("+1 555-0200")
                .designation("HR Director")
                .salary(110000.0)
                .joiningDate(LocalDate.of(2023, 3, 10))
                .status(Employee.EmployeeStatus.ACTIVE)
                .address("200 People Blvd, San Jose, CA")
                .emergencyContact("Juan Martinez: +1 555-0201")
                .user(hrUser)
                .department(hrDept)
                .build();
        hrEmployee = employeeRepository.save(hrEmployee);

        hrDept.setDepartmentHead(hrEmployee);
        departmentRepository.save(hrDept);

        // 5. Initialize Manager Account
        User managerUser = User.builder()
                .username("manager@employeesphere.com")
                .password(passwordEncoder.encode("manager123"))
                .roles(Set.of(managerRole))
                .build();
        managerUser = userRepository.save(managerUser);

        Employee managerEmployee = Employee.builder()
                .employeeId("EMP-003")
                .fullName("Marcus Vance")
                .email("manager@employeesphere.com")
                .phone("+1 555-0300")
                .designation("Engineering Manager")
                .salary(130000.0)
                .joiningDate(LocalDate.of(2022, 6, 20))
                .status(Employee.EmployeeStatus.ACTIVE)
                .address("300 Manager Rd, Redwood City, CA")
                .emergencyContact("Emily Vance: +1 555-0301")
                .user(managerUser)
                .department(engineering)
                .build();
        managerEmployee = employeeRepository.save(managerEmployee);

        // 6. Initialize Standard Employee Account
        User standardUser = User.builder()
                .username("employee@employeesphere.com")
                .password(passwordEncoder.encode("employee123"))
                .roles(Set.of(employeeRole))
                .build();
        standardUser = userRepository.save(standardUser);

        Employee stdEmployee = Employee.builder()
                .employeeId("EMP-004")
                .fullName("Jane Doe")
                .email("employee@employeesphere.com")
                .phone("+1 555-0400")
                .designation("Software Engineer")
                .salary(95000.0)
                .joiningDate(LocalDate.of(2024, 2, 1))
                .status(Employee.EmployeeStatus.ACTIVE)
                .address("400 Coder Court, San Francisco, CA")
                .emergencyContact("John Doe: +1 555-0401")
                .user(standardUser)
                .department(engineering)
                .manager(managerEmployee) // Managed by Marcus Vance
                .build();
        stdEmployee = employeeRepository.save(stdEmployee);

        // Create a few more dummy employees for dynamic dashboards
        createDummyEmployee("EMP-005", "Emma Stone", "emma@employeesphere.com", salesDept, "Account Manager", 85000.0, employeeRole, managerEmployee);
        createDummyEmployee("EMP-006", "David Chen", "david@employeesphere.com", engineering, "Frontend Engineer", 100000.0, employeeRole, managerEmployee);

        // 7. Seed Attendance for testing charts
        LocalDate today = LocalDate.now();
        
        // Seed today's checked-in employees
        checkInDummy(adminEmployee, today, 9, 0); // On-time Present
        checkInDummy(hrEmployee, today, 9, 15); // On-time Present
        checkInDummy(managerEmployee, today, 9, 45); // Late Entry
        checkInDummy(stdEmployee, today, 9, 5); // On-time Present
        
        // Yesterday's complete checks
        LocalDate yesterday = today.minusDays(1);
        checkInOutDummy(adminEmployee, yesterday, 9, 0, 18, 0);
        checkInOutDummy(hrEmployee, yesterday, 8, 45, 17, 30);
        checkInOutDummy(managerEmployee, yesterday, 9, 15, 18, 15);
        checkInOutDummy(stdEmployee, yesterday, 9, 5, 17, 5);

        // 8. Seed Leave Request
        LeaveRequest sickRequest = LeaveRequest.builder()
                .employee(stdEmployee)
                .leaveType(LeaveRequest.LeaveType.SICK)
                .startDate(today.plusDays(2))
                .endDate(today.plusDays(3))
                .reason("Dental surgery recovery")
                .status(LeaveRequest.LeaveStatus.PENDING)
                .createdAt(LocalDateTime.now().minusHours(2))
                .build();
        leaveRequestRepository.save(sickRequest);

        LeaveRequest casualRequest = LeaveRequest.builder()
                .employee(adminEmployee)
                .leaveType(LeaveRequest.LeaveType.CASUAL)
                .startDate(today.minusDays(5))
                .endDate(today.minusDays(4))
                .reason("Family event travel")
                .status(LeaveRequest.LeaveStatus.APPROVED)
                .approvedBy(hrEmployee)
                .createdAt(LocalDateTime.now().minusDays(6))
                .build();
        leaveRequestRepository.save(casualRequest);
    }

    private Role getOrCreateRole(Role.RoleType name) {
        return roleRepository.findByName(name)
                .orElseGet(() -> roleRepository.save(
                        Role.builder().name(name).build()
                ));
    }

    private void createDummyEmployee(String code, String name, String email, Department dept, 
                                     String designation, Double salary, Role role, Employee manager) {
        User user = User.builder()
                .username(email)
                .password(passwordEncoder.encode("user123"))
                .roles(Set.of(role))
                .build();
        user = userRepository.save(user);

        Employee emp = Employee.builder()
                .employeeId(code)
                .fullName(name)
                .email(email)
                .phone("+1 555-0999")
                .designation(designation)
                .salary(salary)
                .joiningDate(LocalDate.now().minusMonths(6))
                .status(Employee.EmployeeStatus.ACTIVE)
                .address("700 Workspace Dr, Tech City")
                .emergencyContact("Friend: +1 555-1111")
                .user(user)
                .department(dept)
                .manager(manager)
                .build();
        employeeRepository.save(emp);
    }

    private void checkInDummy(Employee employee, LocalDate date, int hour, int minute) {
        LocalDateTime checkIn = LocalDateTime.of(date, LocalTime.of(hour, minute));
        Attendance.AttendanceStatus status = checkIn.toLocalTime().isAfter(LocalTime.of(9, 30)) ?
                Attendance.AttendanceStatus.LATE : Attendance.AttendanceStatus.PRESENT;

        Attendance attendance = Attendance.builder()
                .employee(employee)
                .date(date)
                .checkIn(checkIn)
                .status(status)
                .build();
        attendanceRepository.save(attendance);
    }

    private void checkInOutDummy(Employee employee, LocalDate date, int inHr, int inMin, int outHr, int outMin) {
        LocalDateTime checkIn = LocalDateTime.of(date, LocalTime.of(inHr, inMin));
        LocalDateTime checkOut = LocalDateTime.of(date, LocalTime.of(outHr, outMin));
        Attendance.AttendanceStatus status = checkIn.toLocalTime().isAfter(LocalTime.of(9, 30)) ?
                Attendance.AttendanceStatus.LATE : Attendance.AttendanceStatus.PRESENT;

        Duration duration = Duration.between(checkIn, checkOut);
        double workingHours = Math.round((duration.toMinutes() / 60.0) * 100.0) / 100.0;

        Attendance attendance = Attendance.builder()
                .employee(employee)
                .date(date)
                .checkIn(checkIn)
                .checkOut(checkOut)
                .workingHours(workingHours)
                .status(status)
                .build();
        attendanceRepository.save(attendance);
    }
}
