package com.example.EyeCareHubDB.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.EyeCareHubDB.entity.Account;
import com.example.EyeCareHubDB.entity.Feedback;
import com.example.EyeCareHubDB.entity.Feedback.FeedbackStatus;
import com.example.EyeCareHubDB.repository.FeedbackRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class FeedbackService {

    private final FeedbackRepository feedbackRepository;

    @Transactional
    public Feedback createFeedback(Feedback feedback) {
        if (feedback.getRating() < 1 || feedback.getRating() > 5) {
            throw new RuntimeException("Rating must be between 1 and 5");
        }
        feedback.setStatus(FeedbackStatus.NEW);
        return feedbackRepository.save(feedback);
    }

    @Transactional
    public Feedback replyAndModerate(Long id, String reply, Long staffAccountId, FeedbackStatus status) {
        Feedback feedback = feedbackRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Feedback not found: " + id));
        if (reply != null) {
            feedback.setStaffReply(reply);
            feedback.setRepliedBy(Account.builder().id(staffAccountId).build());
            feedback.setRepliedAt(LocalDateTime.now());
        }
        if (status != null) feedback.setStatus(status);
        return feedbackRepository.save(feedback);
    }

    public List<Feedback> getFeedbacksByVariant(Long variantId) {
        return feedbackRepository.findByVariantIdAndStatus(variantId, FeedbackStatus.PUBLISHED);
    }

    public List<Feedback> getPendingFeedbacks() {
        return feedbackRepository.findByStatus(FeedbackStatus.NEW);
    }

    public List<Feedback> getFeedbacksByOrder(Long orderId) {
        return feedbackRepository.findByOrderId(orderId);
    }
}
