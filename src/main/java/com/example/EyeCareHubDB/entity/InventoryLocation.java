package com.example.EyeCareHubDB.entity;

import java.time.LocalDateTime;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "\"InventoryLocations\"")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InventoryLocation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "\"LocationId\"")
    private Long id;

    @Column(name = "\"Name\"", nullable = false, length = 200)
    private String name;

    @Column(name = "\"Code\"", nullable = false, unique = true, length = 50)
    private String code;

    @Enumerated(EnumType.STRING)
    @Column(name = "\"LocationType\"", nullable = false, length = 20)
    private LocationType locationType;

    @Column(name = "\"AddressText\"", length = 500)
    private String address;

    @Column(name = "\"IsActive\"", nullable = false)
    private Boolean isActive = true;

    @Column(name = "\"CreatedAt\"", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Transient // Not in user screenshot for Locations? Keeping as transient for now if not sure, or mapping if exists.
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

    public enum LocationType {
        WAREHOUSE, SHOP
    }
}
