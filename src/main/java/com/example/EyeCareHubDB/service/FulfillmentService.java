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

@Service
@RequiredArgsConstructor
public class FulfillmentService {

    private final FulfillmentTaskRepository taskRepository;
    private final OrderRepository orderRepository;

    @Transactional
    public void generateTasksForOrder(Long orderId) {
        Order order = orderRepository.findById(orderId)
            .orElseThrow(() -> new RuntimeException("Order not found: " + orderId));

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

    @Transactional
    public FulfillmentTaskDTO updateTask(Long taskId, TaskStatus status, Long assignedToId) {
        FulfillmentTask task = taskRepository.findById(taskId)
            .orElseThrow(() -> new RuntimeException("Task not found: " + taskId));
        task.setStatus(status);
        if (status == TaskStatus.IN_PROGRESS && task.getStartedAt() == null) {
            task.setStartedAt(LocalDateTime.now());
        }
        if (status == TaskStatus.DONE) {
            task.setDoneAt(LocalDateTime.now());
        }
        if (assignedToId != null) {
            task.setAssignedTo(Account.builder().id(assignedToId).build());
        }
        return toDTO(taskRepository.save(task));
    }

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
            }
        }
    }

    @Transactional(readOnly = true)
    public List<FulfillmentTaskDTO> getTasksByOrder(Long orderId) {
        return taskRepository.findByOrderIdOrderByCreatedAtAsc(orderId).stream()
            .map(this::toDTO)
            .toList();
    }

    @Transactional(readOnly = true)
    public List<FulfillmentTaskDTO> getMyTasks(Long accountId, TaskStatus status) {
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
            .startedAt(task.getStartedAt())
            .doneAt(task.getDoneAt())
            .createdAt(task.getCreatedAt())
            .updatedAt(task.getUpdatedAt())
            .build();
    }
}
