package com.example.EyeCareHubDB.entity;

import java.time.LocalDateTime;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "shipments")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Shipment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "\"OrderId\"", nullable = false, unique = true)
    private Order order;

    @Column(length = 100)
    private String carrier;

    @Column(length = 200)
    private String trackingNumber;

    @Column(length = 500)
    private String trackingUrl;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ShipmentStatus status = ShipmentStatus.CREATED;

    private LocalDateTime estimatedDelivery;

    private LocalDateTime actualDelivery;

    @Column(columnDefinition = "TEXT")
    private String note;

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

    public enum ShipmentStatus {
        CREATED, IN_TRANSIT, DELIVERED, RETURNED
    }
}
