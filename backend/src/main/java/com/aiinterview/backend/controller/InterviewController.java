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

@RestController
@RequestMapping("/api/interview")
public class InterviewController {

    private final InterviewService interviewService;
    private final UserRepository userRepository;

    public InterviewController(
            InterviewService interviewService,
            UserRepository userRepository) {

        this.interviewService = interviewService;
        this.userRepository = userRepository;
    }

    @GetMapping("/ping")
    public String ping() {
        return "Interview OK";
    }

  @PostMapping("/start")
public ResponseEntity<StartInterviewResponse> startInterview(
        Authentication authentication,
        @Valid @RequestBody StartInterviewRequest request) {

    System.out.println("STEP 1");

    User user = userRepository.findByEmail(authentication.getName())
            .orElseThrow(() -> new RuntimeException("User not found"));

    System.out.println("STEP 2");

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

}