# EmployeeSphere 🌐
> Modern, Enterprise-Grade SaaS HRMS, Attendance, and Employee Workflow Platform.

EmployeeSphere is a highly scalable, FAANG-level Employee Management and Attendance System built with **Java 24**, **Spring Boot 3**, **Spring Security**, and **JWT-HttpOnly Cookie hybrid authentication**. Inspired by modern SaaS platforms like Zoho People, Stripe, and Linear, it features a glassmorphic dark-themed layout, interactive Chart.js analytics, and automated salary & leave request workflows.

---

## 🚀 Key Features

*   **Secure Authentication**: Dual JWT support. Cookies allow seamless Spring Security context integration in Thymeleaf MVC views, while REST API clients can pass tokens in the `Authorization: Bearer` header.
*   **Presence & Attendance Tracker**: Interactive dashboard widgets supporting check-in, check-out, working hours calculation, and late check-in alarms.
*   **Workflows (Leaves & Payroll)**: Submitting leave requests triggers manager alerts. Payout processing generates notifications and releases download-ready payslips.
*   **Department Tree**: Management panels to create departments, map department heads, and track active team sizes.
*   **Rich Analytics**: Fully responsive charts rendering department employee distributions and weekly working hours logs.

---

## 🛠️ Technology Stack

*   **Backend**: Java 24, Spring Boot 3.3.1, Spring Data JPA, Hibernate, MySQL, Maven
*   **Security**: Spring Security 6, JWT (JJWT 0.12.5), BCrypt
*   **Frontend**: Thymeleaf, Tailwind CSS (via CDN), custom CSS3, Vanilla JS, Chart.js, FontAwesome

---

## 📋 Database Schema

Refer to [schema.sql](file:///c:/Users/SAMEER/OneDrive/Desktop/java%20sammu/schema.sql) for database definitions, constraints, and relational mappings:
*   `roles` & `users` (ManyToMany)
*   `employees` (OneToOne user, self-referencing hierarchy)
*   `departments` (ManyToOne head)
*   `attendance`, `leave_requests`, `payrolls`, `notifications` (ManyToOne employee)

---

## 🚦 Getting Started

### 1. Prerequisites
*   JDK 21 or JDK 24 installed
*   Maven 3.8+ installed
*   MySQL server running locally on port `3306`

### 2. Database Initialization
Create a MySQL database named `employeesphere` (or let the app auto-create it if your credentials have permissions):
```sql
CREATE DATABASE employeesphere;
```
Verify or update the connection credentials in [application.properties](file:///c:/Users/SAMEER/OneDrive/Desktop/java%20sammu/src/main/resources/application.properties):
```properties
spring.datasource.url=jdbc:mysql://localhost:3306/employeesphere?createDatabaseIfNotExist=true
spring.datasource.username=root
spring.datasource.password=root
```

### 3. Build & Start the Application
Run the following maven commands in the project root directory:
```bash
mvn clean compile
mvn spring-boot:run
```
The server will boot on `http://localhost:8080`.

### 4. Default Seed Accounts
The database automatically seeds these accounts on first launch for immediate testing:
*   **Admin**: `admin@employeesphere.com` / `admin123`
*   **HR specialist**: `hr@employeesphere.com` / `hr123`
*   **Manager**: `manager@employeesphere.com` / `manager123`
*   **Employee**: `employee@employeesphere.com` / `employee123`

---

## 📄 REST API Documentation

All endpoints return standardized JSON payloads and follow HTTP status guidelines.

### Authentication Endpoints
*   **`POST /api/auth/login`**
    *   *Payload*: `{ "email": "admin@employeesphere.com", "password": "admin123" }`
    *   *Response*: Returns JWT Token (`AuthResponse`)
*   **`POST /api/auth/signup`**
    *   *Payload*: `SignupRequest` DTO
    *   *Response*: Returns created employee details (`EmployeeDto`)

### Employees Endpoints (Requires ADMIN/HR role)
*   **`GET /api/employees?search={query}&page=0&size=10`**: Pageable search results.
*   **`GET /api/employees/{id}`**: Detailed profile lookup.
*   **`POST /api/employees`**: Insert new employee.
*   **`PUT /api/employees/{id}`**: Update employee details.
*   **`DELETE /api/employees/{id}`**: Delete employee profile and matching credentials.

### Attendance Endpoints (Requires Authentication)
*   **`POST /api/attendance/check-in?employeeId={id}`**: Submit a check-in. Returns 400 if already check-in logged.
*   **`POST /api/attendance/check-out?employeeId={id}`**: Complete checking out. Computes daily working hours.
*   **`GET /api/attendance/history/{employeeId}`**: Return attendance log list.

### Leaves & Payroll Endpoints
*   **`POST /api/leaves/apply`**: Submit leave request.
*   **`POST /api/leaves/approve/{id}?approvedById={id}`**: Manager action to approve.
*   **`POST /api/leaves/reject/{id}?rejectedById={id}`**: Manager action to reject.
*   **`POST /api/payroll/generate?employeeId={id}&month={m}&year={y}&allowances={a}&deductions={d}`**: HR action to calculate payroll.
*   **`POST /api/payroll/pay/{id}`**: Disburse salary, marking payroll PAID.

---

## 🔭 Architectural Future Scalability

To upgrade this modular codebase to high-throughput SaaS architectures:
1.  **Distributed Caching**: Plug in `spring-boot-starter-data-redis` and use `@Cacheable` on `EmployeeService#getEmployeeById` to reduce query loads on the MySQL database.
2.  **WebSocket Notifications**: Upgrade `NotificationServiceImpl` to use `SimpMessagingTemplate` so notifications pop up instantly on the user dashboard without page reloads.
3.  **Biometric Attendance integration**: Implement a REST controller accepting facial recognition hashes, verify them against stored hashes, and call `AttendanceService#checkIn(employeeId)`.
4.  **Microservices Refactoring**: Extract `Attendance` and `Payroll` domains into dedicated Spring Boot services communicating via Apache Kafka messages or gRPC.
5.  **Container Deployment (Docker)**:
    Create a `Dockerfile` at the root:
    ```dockerfile
    FROM openjdk:24-jdk-slim
    COPY target/employeesphere-0.0.1-SNAPSHOT.jar app.jar
    ENTRYPOINT ["java", "-jar", "/app.jar"]
    ```
