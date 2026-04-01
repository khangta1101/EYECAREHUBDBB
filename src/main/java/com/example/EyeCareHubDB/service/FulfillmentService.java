package com.example.EyeCareHubDB.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.EyeCareHubDB.dto.FulfillmentTaskDTO;
import com.example.EyeCareHubDB.entity.Account;
import com.example.EyeCareHubDB.entity.FulfillmentTask;
import com.example.EyeCareHubDB.entity.FulfillmentTask.TaskStatus;
import com.example.EyeCareHubDB.entity.FulfillmentTask.TaskType;
import com.example.EyeCareHubDB.entity.Order;
import com.example.EyeCareHubDB.repository.FulfillmentTaskRepository;
import com.example.EyeCareHubDB.repository.OrderRepository;

import lombok.RequiredArgsConstructor;

// ============================================================
// SERVICE: FulfillmentService — Quản lý quy trình hoàn thiện đơn hàng (Fulfillment).
// TaskType (loại công việc):
//   CUT_LENS     → Cắt tròng kính theo đơn thuốc
//   ASSEMBLE     → Lắp ráp kính
//   QC           → Kiểm tra chất lượng
//   RECEIVE_PREORDER → Nhận hàng pre-order về kho
//   PACK         → Đóng gói đơn hàng
//   SHIP         → Bàn giao cho đơn vị vận chuyển
// TaskStatus: PENDING → IN_PROGRESS → DONE
// ============================================================
@Service
@RequiredArgsConstructor
public class FulfillmentService {

    private final FulfillmentTaskRepository taskRepository;
    private final OrderRepository orderRepository;

    // ⭐ TỰ ĐỘNG TẠO TASKS khi đơn được xác nhận (gọi từ PaymentService/OrderService)
    // Prescription item → CUT_LENS + ASSEMBLE + QC
    // Pre-order item    → RECEIVE_PREORDER + QC
    // Mọi đơn           → PACK + SHIP (task chung)
    // Nếu đã có task rồi → bỏ qua (tránh tạo trùng)
    @Transactional
    public void generateTasksForOrder(Long orderId) {
        Order order = orderRepository.findById(orderId)
            .orElseThrow(() -> new RuntimeException("Order not found: " + orderId));

        if (!taskRepository.findByOrderIdOrderByCreatedAtAsc(orderId).isEmpty()) {
            return; // Tasks already generated
        }

        for (com.example.EyeCareHubDB.entity.OrderItem item : order.getItems()) {
            if (Boolean.TRUE.equals(item.getIsPrescription())) {
                // Task for prescription assembly
                createTask(order, item, TaskType.CUT_LENS);
                createTask(order, item, TaskType.ASSEMBLE);
                createTask(order, item, TaskType.QC);
            } else if (item.getPreorderExpectedAt() != null) {
                // Task for pre-order receiving
                createTask(order, item, TaskType.RECEIVE_PREORDER);
                createTask(order, item, TaskType.QC);
            }
        }

        // Generic tasks for the whole order
        createTask(order, null, TaskType.PACK);
        createTask(order, null, TaskType.SHIP);
    }

    private void createTask(Order order, com.example.EyeCareHubDB.entity.OrderItem item, TaskType type) {
        taskRepository.save(FulfillmentTask.builder()
            .order(order)
            .orderItem(item)
            .taskType(type)
            .status(TaskStatus.PENDING)
            .build());
    }

    // Cập nhật task: ghi startedAt khi IN_PROGRESS, doneAt khi DONE.
    // Nếu DONE và đơn đang LAB_PROCESSING/AWAITING_STOCK:
    //   → Kiểm tra tất cả prep tasks (CUT_LENS, ASSEMBLE, RECEIVE_PREORDER) DONE chưa
    //   → Nếu hết → tự động chuyển Order sang PROCESSING
    @Transactional
    public FulfillmentTaskDTO updateTask(Long taskId, TaskStatus status, Long assignedToId, String note) {
        FulfillmentTask task = taskRepository.findById(taskId)
            .orElseThrow(() -> new RuntimeException("Task not found: " + taskId));
        
        if (status != null) {
            task.setStatus(status);
            if (status == TaskStatus.IN_PROGRESS) {
                if (task.getStartedAt() == null) task.setStartedAt(LocalDateTime.now());
                // If moving to IN_PROGRESS, assign to the person who started it if not already assigned
                if (task.getAssignedTo() == null && assignedToId != null) {
                    task.setAssignedTo(Account.builder().id(assignedToId).build());
                }
            }
            if (status == TaskStatus.DONE) {
                task.setDoneAt(LocalDateTime.now());
                
                // If this was a prescription/preorder task, check if the whole order can move to PROCESSING
                Order order = task.getOrder();
                if (order.getStatus() == Order.OrderStatus.LAB_PROCESSING || order.getStatus() == Order.OrderStatus.AWAITING_STOCK) {
                    boolean allPrepDone = taskRepository.findByOrderIdOrderByCreatedAtAsc(order.getId()).stream()
                        .filter(t -> t.getTaskType() == TaskType.CUT_LENS || t.getTaskType() == TaskType.ASSEMBLE || t.getTaskType() == TaskType.RECEIVE_PREORDER)
                        .allMatch(t -> t.getStatus() == TaskStatus.DONE);
                    
                    if (allPrepDone) {
                        order.setStatus(Order.OrderStatus.PROCESSING);
                        orderRepository.save(order);
                    }
                }
            }
        }
        
        if (note != null) {
            task.setNote(note);
        }
        
        if (assignedToId != null && task.getAssignedTo() == null) {
            task.setAssignedTo(Account.builder().id(assignedToId).build());
        }
        return toDTO(taskRepository.save(task));
    }

    // Xử lý khi hàng pre-order về kho. Tự động đánh dấu RECEIVE_PREORDER task DONE
    // và chuyển đơn AWAITING_STOCK → PROCESSING nếu nhận đủ hàng.
    @Transactional
    public void processStockArrival(Long variantId, int qtyReceived) {
        // Find all pending RECEIVE_PREORDER tasks for this variant
        List<FulfillmentTask> tasks = taskRepository.findByTaskTypeAndStatus(TaskType.RECEIVE_PREORDER, TaskStatus.PENDING).stream()
            .filter(t -> t.getOrderItem() != null && t.getOrderItem().getVariant().getId().equals(variantId))
            .toList();

        int remainingQty = qtyReceived;
        for (FulfillmentTask t : tasks) {
            if (remainingQty <= 0) break;
            int orderQty = t.getOrderItem().getQty();
            if (remainingQty >= orderQty) {
                t.setStatus(TaskStatus.DONE);
                t.setDoneAt(LocalDateTime.now());
                t.setNote("Stock arrived, qty recognized: " + orderQty);
                taskRepository.save(t);
                remainingQty -= orderQty;

                // After receiving stock, check if order can move from AWAITING_STOCK to PROCESSING
                Order order = t.getOrder();
                if (order.getStatus() == Order.OrderStatus.AWAITING_STOCK) {
                    boolean allReceived = order.getItems().stream()
                        .filter(item -> item.getPreorderExpectedAt() != null)
                        .allMatch(item -> taskRepository.findByTaskTypeAndStatus(TaskType.RECEIVE_PREORDER, TaskStatus.DONE).stream()
                            .anyMatch(task -> task.getOrderItem() != null && task.getOrderItem().getId().equals(item.getId())));
                    
                    if (allReceived) {
                        order.setStatus(Order.OrderStatus.PROCESSING);
                        orderRepository.save(order);
                    }
                }
            }
        }
    }

    @Transactional(readOnly = true)
    public List<FulfillmentTaskDTO> getTasksByOrder(Long orderId) {
        return taskRepository.findByOrderIdOrderByCreatedAtAsc(orderId).stream()
            .map(this::toDTO)
            .toList();
    }

    // Lấy task của nhân viên: PENDING → bao gồm cả task chưa ai nhận (unassigned)
    @Transactional(readOnly = true)
    public List<FulfillmentTaskDTO> getMyTasks(Long accountId, TaskStatus status) {
        if (status == TaskStatus.PENDING) {
            // Include unassigned tasks that any staff can pick up
            return taskRepository.findAll().stream()
                .filter(t -> t.getStatus() == TaskStatus.PENDING && (t.getAssignedTo() == null || t.getAssignedTo().getId().equals(accountId)))
                .map(this::toDTO)
                .toList();
        }
        return taskRepository.findByAssignedToIdAndStatus(accountId, status).stream()
            .map(this::toDTO)
            .toList();
    }

    public FulfillmentTaskDTO toDTO(FulfillmentTask task) {
        return FulfillmentTaskDTO.builder()
            .id(task.getId())
            .orderId(task.getOrder().getId())
            .orderNo(task.getOrder().getOrderNo())
            .orderItemId(task.getOrderItem() != null ? task.getOrderItem().getId() : null)
            .taskType(task.getTaskType().name())
            .status(task.getStatus().name())
            .assignedToId(task.getAssignedTo() != null ? task.getAssignedTo().getId() : null)
            .assignedToEmail(task.getAssignedTo() != null ? task.getAssignedTo().getEmail() : null)
            .note(task.getNote())
            .productName(task.getOrderItem() != null ? task.getOrderItem().getVariant().getProduct().getName() : null)
            .variantName(task.getOrderItem() != null ? task.getOrderItem().getVariant().getVariantName() : null)
            .startedAt(task.getStartedAt())
            .doneAt(task.getDoneAt())
            .createdAt(task.getCreatedAt())
            .updatedAt(task.getUpdatedAt())
            .build();
    }
}
