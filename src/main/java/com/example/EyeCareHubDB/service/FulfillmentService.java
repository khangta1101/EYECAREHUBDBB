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
import com.example.EyeCareHubDB.entity.Order.OrderType;
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

        List<TaskType> taskTypes;
        if (order.getOrderType() == OrderType.PREORDER) {
            taskTypes = List.of(TaskType.RECEIVE_PREORDER, TaskType.QC, TaskType.PACK, TaskType.SHIP);
        } else if (order.getOrderType() == OrderType.PRESCRIPTION) {
            taskTypes = List.of(TaskType.CUT_LENS, TaskType.ASSEMBLE, TaskType.QC, TaskType.PACK, TaskType.SHIP);
        } else {
            taskTypes = List.of(TaskType.PACK, TaskType.SHIP);
        }

        for (TaskType type : taskTypes) {
            taskRepository.save(FulfillmentTask.builder()
                .order(order).taskType(type).status(TaskStatus.PENDING).build());
        }
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
