package com.example.EyeCareHubDB.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "prescriptions")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Prescription {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_item_id", nullable = false, unique = true)
    private OrderItem orderItem;

    // Right eye (OD)
    @Column(precision = 5, scale = 2)
    private BigDecimal odSphere;

    @Column(precision = 5, scale = 2)
    private BigDecimal odCylinder;

    private Integer odAxis;

    @Column(precision = 5, scale = 2)
    private BigDecimal odAdd;

    // Left eye (OS)
    @Column(precision = 5, scale = 2)
    private BigDecimal osSphere;

    @Column(precision = 5, scale = 2)
    private BigDecimal osCylinder;

    private Integer osAxis;

    @Column(precision = 5, scale = 2)
    private BigDecimal osAdd;

    // Pupillary Distance
    @Column(precision = 5, scale = 2)
    private BigDecimal pdRight;

    @Column(precision = 5, scale = 2)
    private BigDecimal pdLeft;

    @Column(length = 500)
    private String prescriptionFileUrl;

    @Column(columnDefinition = "TEXT")
    private String notes;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
