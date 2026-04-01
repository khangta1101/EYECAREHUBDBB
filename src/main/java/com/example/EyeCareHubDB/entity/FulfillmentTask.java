package com.example.EyeCareHubDB.entity;

import java.time.LocalDateTime;

import jakarta.persistence.*;
import lombok.*;

// ============================================================
// ENTITY: FulfillmentTask — Nhiệm vụ sản xuất/xử lý đơn hàng nội bộ.
// orderItem có thể null (PACK, SHIP là task chung cho cả đơn).
// assignedTo: nhân viên được giao việc (null = chưa phân công, ai cũng có thể nhận).
// startedAt: lúc nhân viên bắt đầu làm. doneAt: lúc hoàn thành.
// ============================================================
@Entity
@Table(name = "FulfillmentTasks")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FulfillmentTask {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "\"FulfillmentTaskId\"")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "\"OrderId\"", nullable = false)
    private Order order;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "\"OrderItemId\"")
    private OrderItem orderItem;

    @Enumerated(EnumType.STRING)
    @Column(name = "\"TaskType\"", nullable = false, length = 30)
    private TaskType taskType;

    @Enumerated(EnumType.STRING)
    @Column(name = "\"Status\"", nullable = false, length = 20)
    private TaskStatus status = TaskStatus.PENDING;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "\"AssignedTo\"")
    private Account assignedTo;

    @Column(name = "\"Note\"", columnDefinition = "TEXT")
    private String note;

    @Column(name = "\"StartedAt\"")
    private LocalDateTime startedAt;

    @Column(name = "\"DoneAt\"")
    private LocalDateTime doneAt;

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

    // loại nhiệm vụ: RECEIVE_PREORDER=nhận hàng về, CUT_LENS=cắt tròng, ASSEMBLE=lắp ráp, QC=kiểm tra, PACK=đóng gói, SHIP=giao
    public enum TaskType {
        RECEIVE_PREORDER, CUT_LENS, ASSEMBLE, QC, PACK, SHIP
    }

    // TaskStatus: PENDING=chờ, IN_PROGRESS=đang làm, DONE=xong, CANCELLED=hủy bỏ
    public enum TaskStatus {
        PENDING, IN_PROGRESS, DONE, CANCELLED
    }
}
