package com.aiinterview.backend.controller;

import com.aiinterview.backend.dto.interview.InterviewQuestionRequest;
import com.aiinterview.backend.dto.interview.InterviewQuestionResponse;
import com.aiinterview.backend.dto.interview.InterviewResultResponse;
import com.aiinterview.backend.dto.interview.StartInterviewRequest;
import com.aiinterview.backend.dto.interview.StartInterviewResponse;
import com.aiinterview.backend.entity.User;
import com.aiinterview.backend.repository.UserRepository;
import com.aiinterview.backend.service.interview.InterviewService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import com.aiinterview.backend.dto.interview.InterviewProgressResponse;
import org.springframework.web.bind.annotation.*;

import com.aiinterview.backend.repository.InterviewSessionRepository;
import com.aiinterview.backend.service.EntitlementService;
import org.springframework.http.HttpStatus;
import java.util.Map;

@RestController
@RequestMapping("/api/interview")
public class InterviewController {

    private final InterviewService interviewService;
    private final UserRepository userRepository;
    private final InterviewSessionRepository interviewSessionRepository;
    private final EntitlementService entitlementService;

    public InterviewController(
            InterviewService interviewService,
            UserRepository userRepository,
            InterviewSessionRepository interviewSessionRepository,
            EntitlementService entitlementService) {

        this.interviewService = interviewService;
        this.userRepository = userRepository;
        this.interviewSessionRepository = interviewSessionRepository;
        this.entitlementService = entitlementService;
    }


  @PostMapping("/start")
public ResponseEntity<?> startInterview(
        Authentication authentication,
        @Valid @RequestBody StartInterviewRequest request) {

    User user = userRepository.findByEmail(authentication.getName())
            .orElseThrow(() -> new RuntimeException("User not found"));

    int maxInterviews = entitlementService.getMaxMockInterviews(user);
    long currentInterviews = interviewSessionRepository.countByUser(user);

    if (currentInterviews >= maxInterviews) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(
                Map.of(
                        "status", 403,
                        "error", "PLAN_LIMIT_REACHED",
                        "message", "You have reached the maximum allowed Mock Interviews (" + maxInterviews + ") for your plan. Please upgrade to continue."
                )
        );
    }

    return ResponseEntity.ok(
            interviewService.startInterview(user, request)
    );
}
    @PostMapping("/submit")
    public ResponseEntity<InterviewQuestionResponse> submitAnswer(
            Authentication authentication,
            @Valid @RequestBody InterviewQuestionRequest request) {

        try {

            User user = userRepository.findByEmail(authentication.getName())
                    .orElseThrow(() -> new RuntimeException("User not found"));

            return ResponseEntity.ok(
                    interviewService.submitAnswer(user, request)
            );

        } catch (Exception e) {

            e.printStackTrace();
            throw e;
        }
    }


    @PostMapping("/end/{sessionId}")
public ResponseEntity<?> endInterview(
        Authentication authentication,
        @PathVariable Long sessionId) {

    try {

        User user = userRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));

        interviewService.endInterview(sessionId, user);

        return ResponseEntity.ok().build();

    } catch (Exception e) {

        e.printStackTrace();

        throw e;
    }
}
    

    @GetMapping("/result/{sessionId}")
public ResponseEntity<InterviewResultResponse> getInterviewResult(
        Authentication authentication,
        @PathVariable Long sessionId) {

    User user = userRepository.findByEmail(authentication.getName())
            .orElseThrow(() -> new RuntimeException("User not found"));

    return ResponseEntity.ok(
            interviewService.getInterviewResult(user, sessionId)
    );
}

@GetMapping("/progress/{sessionId}")
public ResponseEntity<InterviewProgressResponse> getInterviewProgress(
        Authentication authentication,
        @PathVariable Long sessionId) {

    User user = userRepository.findByEmail(authentication.getName())
            .orElseThrow(() -> new RuntimeException("User not found"));

    return ResponseEntity.ok(
            interviewService.getInterviewProgress(user, sessionId)
    );
}

    @GetMapping("/count")
    public ResponseEntity<Long> getCompletedInterviewCount(
            Authentication authentication) {
        User user = userRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));
        return ResponseEntity.ok(
                interviewService.getCompletedInterviewCount(user)
        );
    }

}