package com.example.EyeCareHubDB.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import jakarta.persistence.*;
import lombok.*;

// ============================================================
// ENTITY: CartItem — 1 sản phẩm trong giỏ hàng.
// ⭐ unitPriceSnap: snapshot giá tại thời điểm nhấn "Thêm vào giỏ" (chốt giá, không đổi dù giá sản phẩm thay đổi sau).
// isPreorder=true → bỏ qua kiểm tra kho, cho phép đặt hàng dù hết kho.
// prescriptionId: ID của đơn thuốc gắn kèm (nếu có). Lưu bằng Long, không phải FK JPA.
// ============================================================
@Entity
@Table(name = "CartItems")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CartItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "\"CartItemId\"")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "\"CartId\"", nullable = false)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Cart cart;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "\"VariantId\"", nullable = false)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private ProductVariant variant;

    @Column(name = "\"Qty\"", nullable = false)
    private Integer qty;

    @Column(name = "\"UnitPriceSnap\"", nullable = false, precision = 10, scale = 2)
    private BigDecimal unitPriceSnap;

    @Column(name = "\"IsPreorder\"", nullable = false)
    @Builder.Default
    private Boolean isPreorder = false;

    @Column(name = "\"PreorderExpectedAt\"")
    private LocalDateTime preorderExpectedAt;

    @Column(name = "\"PrescriptionId\"")
    private Long prescriptionId;

    @Column(name = "\"AddedAt\"", nullable = false, updatable = false)
    private LocalDateTime addedAt;

    @PrePersist
    protected void onCreate() {
        addedAt = LocalDateTime.now();
    }
}
