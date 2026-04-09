package com.example.EyeCareHubDB.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.example.EyeCareHubDB.entity.FulfillmentTask;
import com.example.EyeCareHubDB.entity.FulfillmentTask.TaskStatus;

@Repository
public interface FulfillmentTaskRepository extends JpaRepository<FulfillmentTask, Long> {
    List<FulfillmentTask> findByOrderIdOrderByCreatedAtAsc(Long orderId);
    List<FulfillmentTask> findByAssignedToIdAndStatus(Long accountId, TaskStatus status);
    List<FulfillmentTask> findByStatus(TaskStatus status);
    List<FulfillmentTask> findByTaskTypeAndStatus(FulfillmentTask.TaskType type, TaskStatus status);

    // Query hiệu suất: lấy tasks PENDING (chưa ai nhận HOẾ́C của accountId này)
    // Thay thế findAll().stream().filter() — tránh load toàn bộ bảng
    @Query("SELECT t FROM FulfillmentTask t WHERE t.status = 'PENDING' AND (t.assignedTo IS NULL OR t.assignedTo.id = :accountId)")
    List<FulfillmentTask> findPendingTasksForAccount(@org.springframework.data.repository.query.Param("accountId") Long accountId);
}
