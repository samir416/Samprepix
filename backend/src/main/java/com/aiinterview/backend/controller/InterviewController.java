package com.aiinterview.backend.controller;

import com.aiinterview.backend.dto.interview.InterviewQuestionRequest;
import com.aiinterview.backend.dto.interview.InterviewQuestionResponse;
import com.aiinterview.backend.dto.interview.StartInterviewRequest;
import com.aiinterview.backend.dto.interview.StartInterviewResponse;
import com.aiinterview.backend.entity.User;
import com.aiinterview.backend.service.interview.InterviewService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/interview")
public class InterviewController {

    private final InterviewService interviewService;

    public InterviewController(InterviewService interviewService) {
        this.interviewService = interviewService;
    }

    @PostMapping("/start")
    public ResponseEntity<StartInterviewResponse> startInterview(
            @AuthenticationPrincipal User user,
            @Valid @RequestBody StartInterviewRequest request) {

        return ResponseEntity.ok(
                interviewService.startInterview(user, request)
        );
    }

    @PostMapping("/submit")
    public ResponseEntity<InterviewQuestionResponse> submitAnswer(
            @AuthenticationPrincipal User user,
            @Valid @RequestBody InterviewQuestionRequest request) {

        return ResponseEntity.ok(
                interviewService.submitAnswer(user, request)
        );
    }
}