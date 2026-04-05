package com.example.EyeCareHubDB.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

// ============================================================
// ENTITY: Order — Đơn hàng. Bảng trung tâm của toàn bộ dòng chảy nghiệp vụ.
// OrderType: IN_STOCK (hàng có sẵn) | PREORDER (đặt trước) | PRESCRIPTION (kính thuốc)
// grandTotal = subtotal - discountTotal + shippingFee
// orderNo: mã đơn hàng duy nhất (UUID ngắn). items: cascade ALL (xóa đơn → xóa item)
// ============================================================
@Entity
@Table(name = "Orders")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "OrderId")
    private Long id;

    @Column(name = "OrderNo", nullable = false, unique = true, length = 50)
    private String orderNo;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "CustomerId", nullable = false)
    private Customer customer;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ShippingAddressId")
    private Address shippingAddress;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "SalesStaffId")
    private Account salesStaff;

    @Enumerated(EnumType.STRING)
    @Column(name = "Channel", nullable = false, length = 20)
    @Builder.Default
    private Channel channel = Channel.ONLINE;

    @Enumerated(EnumType.STRING)
    @Column(name = "OrderType", nullable = false, length = 20)
    private OrderType orderType;

    @Enumerated(EnumType.STRING)
    @Column(name = "Status", nullable = false, length = 20)
    @Builder.Default
    private OrderStatus status = OrderStatus.NEW;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "PromotionId")
    private Promotion promotion;

    // subtotal = tổng tiền hàng (chưa giảm, chưa ship)
    @Column(name = "Subtotal", nullable = false, precision = 12, scale = 2)
    private BigDecimal subtotal;

    // discountTotal = số tiền được giảm từ mã khuyến mãi (0 nếu không có mã)
    @Column(name = "DiscountTotal", nullable = false, precision = 12, scale = 2)
    @Builder.Default
    private BigDecimal discountTotal = BigDecimal.ZERO;

    // shippingFee = phí vận chuyển (mặc định 30.000đ, FREE_SHIPPING → 0)
    @Column(name = "ShippingFee", nullable = false, precision = 10, scale = 2)
    @Builder.Default
    private BigDecimal shippingFee = BigDecimal.ZERO;

    // grandTotal = subtotal - discountTotal + shippingFee (số tiền khách thực trả)
    @Column(name = "GrandTotal", nullable = false, precision = 12, scale = 2)
    private BigDecimal grandTotal;

    @Column(name = "Note", columnDefinition = "TEXT")
    private String note;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<OrderItem> items = new ArrayList<>();

    @OneToMany(mappedBy = "order")
    @Builder.Default
    private List<Payment> payments = new ArrayList<>();

    @Column(name = "CreatedAt", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "UpdatedAt")
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

    // ONLINE=đơn từ web/app. OFFLINE=đơn do nhân viên nhập tay tại cửa hàng.
    public enum Channel {
        ONLINE, OFFLINE
    }

    // Loại đơn: IN_STOCK=kho sẵn, PREORDER=đặt trước hàng chưa về, PRESCRIPTION=kính cắt theo tòa.
    public enum OrderType {
        IN_STOCK, PREORDER, PRESCRIPTION
    }

    // ⭐ STATE MACHINE: NEW→CONFIRMED→PROCESSING→SHIPPED→COMPLETED (trình tự chuẩn)
    // AWAITING_STOCK: chờ hàng pre-order về. LAB_PROCESSING: đang cắt/lắp kính thuốc.
    // WAITING_STOCK: giá trị legacy cũ trong DB, giữ để tương thích dữ liệu cũ.
    // CANCELLED/REFUNDED: trạng thái kết thúc (không chuyển tiếp và u được).
    public enum OrderStatus {
        NEW, CONFIRMED, PROCESSING, SHIPPED, COMPLETED, CANCELLED, REFUNDED, AWAITING_STOCK, WAITING_STOCK, LAB_PROCESSING
    }
}
