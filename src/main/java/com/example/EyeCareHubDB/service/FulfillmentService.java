package com.example.EyeCareHubDB.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

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
    private final FileService fileService;

    @Transactional
    public void generateTasksForOrder(Long orderId) {
        Order order = orderRepository.findById(orderId)
            .orElseThrow(() -> new RuntimeException("Order not found: " + orderId));

        List<TaskType> taskTypes = List.of(
            TaskType.CUT_LENS, TaskType.ASSEMBLE, TaskType.QC,
            TaskType.PACK, TaskType.SHIP
        );

        for (TaskType type : taskTypes) {
            taskRepository.save(FulfillmentTask.builder()
                .order(order).taskType(type).status(TaskStatus.PENDING).build());
        }
    }

    @Transactional
    public FulfillmentTask updateTask(Long taskId, TaskStatus status, Long assignedToId) {
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
        return taskRepository.save(task);
    }

    public List<FulfillmentTask> getTasksByOrder(Long orderId) {
        return taskRepository.findByOrderIdOrderByCreatedAtAsc(orderId);
    }

    public List<FulfillmentTask> getMyTasks(Long accountId, TaskStatus status) {
        return taskRepository.findByAssignedToIdAndStatus(accountId, status);
    }

    // Upload ảnh bằng chứng cho task (ảnh cắt tròng, lắp ráp, kiểm tra QC, v.v.)
    @Transactional
    public FulfillmentTask uploadEvidenceImage(Long taskId, MultipartFile imageFile) {
        FulfillmentTask task = taskRepository.findById(taskId)
            .orElseThrow(() -> new RuntimeException("Task not found: " + taskId));

        if (imageFile != null && !imageFile.isEmpty()) {
            String imageUrl = fileService.saveFile(imageFile, "fulfillment-evidence");
            task.setEvidenceImageUrl(imageUrl);
            taskRepository.save(task);
        }

        return task;
    }
}
