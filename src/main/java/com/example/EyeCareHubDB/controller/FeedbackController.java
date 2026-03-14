package com.example.EyeCareHubDB.controller;

import io.swagger.v3.oas.annotations.tags.Tag;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.example.EyeCareHubDB.entity.Feedback;
import com.example.EyeCareHubDB.entity.Feedback.FeedbackStatus;
import com.example.EyeCareHubDB.service.FeedbackService;

import lombok.RequiredArgsConstructor;

@Tag(name = "Feedback")
@RestController
@RequestMapping("/api/feedbacks")
@RequiredArgsConstructor
public class FeedbackController {

    private final FeedbackService feedbackService;

    @PostMapping
    public ResponseEntity<Feedback> create(@RequestBody Feedback feedback) {
        return ResponseEntity.ok(feedbackService.createFeedback(feedback));
    }

    @GetMapping("/variant/{variantId}")
    public ResponseEntity<List<Feedback>> getByVariant(@PathVariable("variantId") Long variantId) {
        return ResponseEntity.ok(feedbackService.getFeedbacksByVariant(variantId));
    }

    @GetMapping("/order/{orderId}")
    public ResponseEntity<List<Feedback>> getByOrder(@PathVariable("orderId") Long orderId) {
        return ResponseEntity.ok(feedbackService.getFeedbacksByOrder(orderId));
    }

    @GetMapping("/pending")
    public ResponseEntity<List<Feedback>> getPending() {
        return ResponseEntity.ok(feedbackService.getPendingFeedbacks());
    }

    @PatchMapping("/{id}/moderate")
    public ResponseEntity<Feedback> moderate(@PathVariable("id") Long id,
                                              @RequestParam(name = "reply", required = false) String reply,
                                              @RequestParam("staffAccountId") Long staffAccountId,
                                              @RequestParam(name = "status", required = false) FeedbackStatus status) {
        return ResponseEntity.ok(feedbackService.replyAndModerate(id, reply, staffAccountId, status));
    }
}
