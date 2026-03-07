package com.example.EyeCareHubDB.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.EyeCareHubDB.entity.FulfillmentTask;
import com.example.EyeCareHubDB.entity.FulfillmentTask.TaskStatus;

@Repository
public interface FulfillmentTaskRepository extends JpaRepository<FulfillmentTask, Long> {
    List<FulfillmentTask> findByOrderIdOrderByCreatedAtAsc(Long orderId);
    List<FulfillmentTask> findByAssignedToIdAndStatus(Long accountId, TaskStatus status);
    List<FulfillmentTask> findByStatus(TaskStatus status);
}
