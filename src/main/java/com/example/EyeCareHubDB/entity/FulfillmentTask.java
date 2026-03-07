package com.example.EyeCareHubDB.entity;

import java.time.LocalDateTime;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "fulfillment_tasks")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FulfillmentTask {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_item_id")
    private OrderItem orderItem;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private TaskType taskType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TaskStatus status = TaskStatus.PENDING;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assigned_to")
    private Account assignedTo;

    @Column(columnDefinition = "TEXT")
    private String note;

    private LocalDateTime startedAt;

    private LocalDateTime doneAt;

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

    public enum TaskType {
        RECEIVE_PREORDER, CUT_LENS, ASSEMBLE, QC, PACK, SHIP
    }

    public enum TaskStatus {
        PENDING, IN_PROGRESS, DONE, CANCELLED
    }
}
