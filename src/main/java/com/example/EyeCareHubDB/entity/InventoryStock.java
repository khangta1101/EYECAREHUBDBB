package com.example.EyeCareHubDB.entity;

import java.time.LocalDateTime;

import jakarta.persistence.*;
import lombok.*;

// ============================================================
// ENTITY: InventoryStock — Tồn kho của 1 biến thể (variant) tại 1 vị trí kho (location).
// Khóa chính tổng hợp: (LocationId, VariantId) — @IdClass(InventoryStockId)
// onHandQty  = số lượng vật lý đang có trong kho
// reservedQty = số lượng đã bị giữ bởi đơn hàng chưa giao (chưa xuất kho)
// availableQty = onHandQty - reservedQty (được tính toán, không lưu DB)
// ============================================================
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

    // ⭐ availableQty = onHandQty - reservedQty (hàng có thể bán được ngay)
    public Integer getAvailableQty() {
        return onHandQty - reservedQty;
    }
}
