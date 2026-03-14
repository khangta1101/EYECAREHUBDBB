package com.example.EyeCareHubDB.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "accounts")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Account {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "\"AccountId\"")
    private Long id;
    
    @Column(name = "\"Email\"", unique = true, nullable = false, length = 100)
    private String email;

    @Column(name = "\"Username\"", unique = true, length = 80)
    private String username;
    
    @Column(name = "\"PasswordHash\"", nullable = false)
    private String passwordHash;
    
    @Transient
    private String phoneNumber;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "\"RoleCode\"", nullable = false, length = 20)
    private AccountRole role ;
    
    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(name = "\"Status\"", nullable = false, length = 20)
    private AccountStatus status = AccountStatus.ACTIVE;
    
    @Builder.Default
    @Column(name = "\"CreatedAt\"", updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();
    
    @Builder.Default
    @Transient
    private LocalDateTime updatedAt = LocalDateTime.now();
    
    @Column(name = "\"LastLoginAt\"")
    private LocalDateTime lastLoginAt;
    
    @Transient
    private Customer customer;
    
    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
        if (updatedAt == null) {
            updatedAt = LocalDateTime.now();
        }
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
    
    public enum AccountRole {
        CUSTOMER, ADMIN, STAFF, MANAGER, OPERATIONS_STAFF
    }
    
    public enum AccountStatus {
        ACTIVE, INACTIVE, SUSPENDED, DELETED
    }
}
