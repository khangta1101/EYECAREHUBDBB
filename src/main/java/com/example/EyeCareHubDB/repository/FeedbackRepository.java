package com.example.EyeCareHubDB.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.EyeCareHubDB.entity.Feedback;
import com.example.EyeCareHubDB.entity.Feedback.FeedbackStatus;

@Repository
public interface FeedbackRepository extends JpaRepository<Feedback, Long> {
    List<Feedback> findByVariantIdAndStatus(Long variantId, FeedbackStatus status);
    List<Feedback> findByOrderId(Long orderId);
    List<Feedback> findByStatus(FeedbackStatus status);
    List<Feedback> findByCustomerId(Long customerId);
}
