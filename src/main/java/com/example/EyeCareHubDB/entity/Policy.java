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
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "Policies")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Policy {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "PolicyId")
    private Long id;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "PolicyType", nullable = false, length = 50)
    private PolicyType type;

    @Column(name = "Slug", length = 200)
    private String slug;
    
    @Column(name = "Title", nullable = false, length = 200)
    private String title;
    
    @Column(name = "Content", columnDefinition = "TEXT", nullable = false)
    private String content;

    @Column(name = "Version", nullable = false)
    @Builder.Default
    private Integer version = 1;

    @Column(name = "EffectiveFrom", nullable = false)
    @Builder.Default
    private LocalDateTime effectiveFrom = LocalDateTime.now();

    @Column(name = "EffectiveTo")
    private LocalDateTime effectiveTo;
    
    @Builder.Default
    @Column(name = "IsActive", nullable = false)
    private Boolean isActive = true;
    
    @Column(name = "CreatedBy", length = 100)
    private String createdBy;

    @Column(name = "CreatedAt", nullable = false, updatable = false)
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();
    
    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }
    
    public enum PolicyType {
        RETURN,
        WARRANTY,
        SHIPPING,
        PAYMENT,
        PRIVACY,
        TERMS,
        OTHER
    }
}
