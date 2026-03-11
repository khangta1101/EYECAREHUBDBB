package com.example.EyeCareHubDB.entity;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.PostLoad;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "customers")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Customer {
    
    @Id
    @Column(name = "\"CustomerId\"")
    private Long id;
    
    @Transient
    private Account account;
    
    @Column(name = "\"FullName\"", nullable = false, length = 200)
    private String fullName;

    @Transient
    private String firstName;
    
    @Transient
    private String lastName;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "\"Gender\"", length = 10)
    private Gender gender;
    
    @Column(name = "\"DateOfBirth\"")
    private LocalDate dateOfBirth;
    
    @Column(name = "\"AvatarUrl\"", length = 500)
    private String avatarUrl;
    
    @Transient
    private List<Address> addresses = new ArrayList<>();
    
    @Column(name = "\"CreatedAt\"", nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();
    
    @Transient
    private LocalDateTime updatedAt = LocalDateTime.now();
    
    @PrePersist
    protected void onCreate() {
        syncFullName();
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
        if (updatedAt == null) {
            updatedAt = LocalDateTime.now();
        }
    }
    
    @PreUpdate
    protected void onUpdate() {
        syncFullName();
        updatedAt = LocalDateTime.now();
    }

    @PostLoad
    protected void onLoad() {
        splitFullName();
    }

    private void syncFullName() {
        String first = firstName == null ? "" : firstName.trim();
        String last = lastName == null ? "" : lastName.trim();
        String combined = (first + " " + last).trim();
        if (combined.isEmpty()) {
            combined = fullName;
        }
        fullName = combined;
    }

    private void splitFullName() {
        if (fullName == null || fullName.isBlank()) {
            firstName = null;
            lastName = null;
            return;
        }
        String[] parts = fullName.trim().split("\\s+", 2);
        firstName = parts[0];
        lastName = parts.length > 1 ? parts[1] : "";
    }
    
    public enum Gender {
        MALE, FEMALE, OTHER
    }
}
