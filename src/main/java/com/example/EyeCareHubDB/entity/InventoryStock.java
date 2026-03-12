package com.example.EyeCareHubDB.entity;

import java.time.LocalDateTime;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "\"InventoryStocks\"")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@IdClass(InventoryStockId.class)
public class InventoryStock {

    @Id
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "\"LocationId\"", nullable = false)
    private InventoryLocation location;

    @Id
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "\"VariantId\"", nullable = false)
    private ProductVariant variant;

    @Builder.Default
    @Column(name = "\"OnHandQty\"", nullable = false)
    private Integer onHandQty = 0;

    @Builder.Default
    @Column(name = "\"ReservedQty\"", nullable = false)
    private Integer reservedQty = 0;

    @Transient // Not in screenshot for Stocks, but exists for Locations. Let's make it transient to be safe or check.
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

    public Integer getAvailableQty() {
        return onHandQty - reservedQty;
    }
}
