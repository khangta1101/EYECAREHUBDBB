package com.example.EyeCareHubDB.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "OrderItems")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "OrderItemId")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "OrderId", nullable = false)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Order order;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "VariantId", nullable = false)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private ProductVariant variant;

    @Column(name = "Qty", nullable = false)
    private Integer qty;

    @Column(name = "UnitPrice", nullable = false, precision = 10, scale = 2)
    private BigDecimal unitPrice;

    @Column(name = "LineTotal", nullable = false, precision = 12, scale = 2)
    private BigDecimal lineTotal;

    @Builder.Default
    @Column(name = "IsPrescription", nullable = false)
    private Boolean isPrescription = false;

    @Column(name = "PreorderExpectedAt")
    private LocalDateTime preorderExpectedAt;

    @Column(name = "PreorderReceivedAt")
    private LocalDateTime preorderReceivedAt;

    @Column(name = "ItemNote", length = 500)
    private String itemNote;

    @OneToOne(mappedBy = "orderItem", cascade = CascadeType.ALL, orphanRemoval = true)
    private Prescription prescription;
}
