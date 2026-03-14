package com.example.EyeCareHubDB.entity;

import java.time.LocalDateTime;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "Shipments")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Shipment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "\"ShipmentId\"")
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "\"OrderId\"", nullable = false, unique = true)
    private Order order;

    @Column(name = "\"Carrier\"", length = 100)
    private String carrier;

    @Column(name = "\"TrackingNo\"", length = 200)
    private String trackingNumber;

    @Column(name = "\"TrackingUrl\"", length = 500)
    private String trackingUrl;

    @Enumerated(EnumType.STRING)
    @Column(name = "\"Status\"", nullable = false, length = 20)
    private ShipmentStatus status = ShipmentStatus.CREATED;

    @Column(name = "\"EstimatedDelivery\"")
    private LocalDateTime estimatedDelivery;

    @Column(name = "\"ShippedAt\"")
    private LocalDateTime shippedAt;

    @Column(name = "\"DeliveredAt\"")
    private LocalDateTime actualDelivery;

    @Column(name = "\"Note\"", columnDefinition = "TEXT")
    private String note;

    @Column(name = "\"CreatedAt\"", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "\"UpdatedAt\"", nullable = false)
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
