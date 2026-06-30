package com.employeesphere.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "roles")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Role {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(length = 20, unique = true, nullable = false)
    private RoleType name;

    public enum RoleType {
        ROLE_ADMIN,
        ROLE_HR,
        ROLE_MANAGER,
        ROLE_EMPLOYEE
    }
}
