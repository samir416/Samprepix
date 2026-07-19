package com.aiinterview.backend.controller;

import com.aiinterview.backend.dto.feedback.FeedbackRequest;
import com.aiinterview.backend.dto.feedback.FeedbackResponse;
import com.aiinterview.backend.entity.FeedbackApprovalToken;
import com.aiinterview.backend.entity.FeedbackStatus;
import com.aiinterview.backend.entity.InterviewFeedback;
import com.aiinterview.backend.entity.User;
import com.aiinterview.backend.repository.InterviewFeedbackRepository;
import com.aiinterview.backend.repository.UserRepository;
import com.aiinterview.backend.service.feedback.FeedbackApprovalTokenService;
import com.aiinterview.backend.service.feedback.InterviewFeedbackService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/feedback")
public class InterviewFeedbackController {

    private final InterviewFeedbackService interviewFeedbackService;
    private final FeedbackApprovalTokenService tokenService;
    private final InterviewFeedbackRepository interviewFeedbackRepository;
    private final UserRepository userRepository;

    public InterviewFeedbackController(
            InterviewFeedbackService interviewFeedbackService,
            FeedbackApprovalTokenService tokenService,
            InterviewFeedbackRepository interviewFeedbackRepository,
            UserRepository userRepository) {

        this.interviewFeedbackService = interviewFeedbackService;
        this.tokenService = tokenService;
        this.interviewFeedbackRepository = interviewFeedbackRepository;
        this.userRepository = userRepository;
    }

    @PostMapping("/submit")
    public ResponseEntity<FeedbackResponse> submitFeedback(
            Authentication authentication,
            @Valid @RequestBody FeedbackRequest request) {

        User user = userRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));

        return ResponseEntity.ok(
                interviewFeedbackService.submitFeedback(user, request)
        );
    }

    @GetMapping("/approve")
    public ResponseEntity<String> approveFeedback(
            @RequestParam String token) {

        FeedbackApprovalToken approvalToken =
                tokenService.validateToken(token);

        InterviewFeedback feedback =
                approvalToken.getFeedback();

        feedback.setStatus(FeedbackStatus.APPROVED);

        interviewFeedbackRepository.save(feedback);

        tokenService.markAsUsed(approvalToken);

        return ResponseEntity.ok(
                "✅ Feedback approved successfully.");
    }

    @GetMapping("/reject")
    public ResponseEntity<String> rejectFeedback(
            @RequestParam String token) {

        FeedbackApprovalToken approvalToken =
                tokenService.validateToken(token);

        InterviewFeedback feedback =
                approvalToken.getFeedback();

        tokenService.deleteToken(approvalToken);

        interviewFeedbackRepository.delete(feedback);

        return ResponseEntity.ok(
                "❌ Feedback rejected and deleted successfully.");
    }

    @GetMapping("/public")
    public ResponseEntity<Page<InterviewFeedback>> getApprovedFeedback(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        return ResponseEntity.ok(
                interviewFeedbackService.getApprovedFeedback(page, size)
        );
    }
}