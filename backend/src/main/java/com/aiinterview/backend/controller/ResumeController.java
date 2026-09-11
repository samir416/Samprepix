package com.aiinterview.backend.controller;

import com.aiinterview.backend.entity.ResumeAnalysis;
import com.aiinterview.backend.model.ResumeResponse;
import com.aiinterview.backend.service.ResumeServiceV2;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

import com.aiinterview.backend.entity.User;
import com.aiinterview.backend.repository.ResumeAnalysisRepository;
import com.aiinterview.backend.repository.UserRepository;
import com.aiinterview.backend.service.EntitlementService;
import org.springframework.http.HttpStatus;
import java.util.Map;

@RestController
@RequestMapping("/resume")
public class ResumeController {

    private final ResumeServiceV2 resumeService;
    private final EntitlementService entitlementService;
    private final UserRepository userRepository;
    private final ResumeAnalysisRepository resumeAnalysisRepository;

    public ResumeController(
            ResumeServiceV2 resumeService,
            EntitlementService entitlementService,
            UserRepository userRepository,
            ResumeAnalysisRepository resumeAnalysisRepository) {
        this.resumeService = resumeService;
        this.entitlementService = entitlementService;
        this.userRepository = userRepository;
        this.resumeAnalysisRepository = resumeAnalysisRepository;
    }

    /**
     * Analyze Resume
     */
    @PostMapping("/analyze")
    public ResponseEntity<?> analyzeResume(
            @RequestParam("file") MultipartFile file,
            HttpServletRequest request) throws Exception {

        String email = (String) request.getAttribute("email");
        if (email == null || email.isBlank()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
                    Map.of("message", "Unauthorized request")
            );
        }

        User user = userRepository.findByEmail(email).orElse(null);
        if (user != null) {
            int maxScans = entitlementService.getMaxResumeScans(user);
            long currentScans = resumeAnalysisRepository.countByUserEmail(email);
            if (currentScans >= maxScans) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(
                        Map.of(
                                "status", 403,
                                "error", "PLAN_LIMIT_REACHED",
                                "message", "You have reached the maximum allowed Resume Scans (" + maxScans + ") for your plan. Please upgrade to continue."
                        )
                );
            }
        }

        ResumeResponse response =
                resumeService.analyzeResumeFile(file, email);

        return ResponseEntity.ok(response);
    }

    /**
     * Resume History
     */
    @GetMapping("/history")
    public ResponseEntity<List<ResumeAnalysis>> getHistory(
            HttpServletRequest request) {

        String email = (String) request.getAttribute("email");

        return ResponseEntity.ok(
                resumeService.getHistory(email)
        );
    }

    /**
     * Latest Resume Analysis
     */
    @GetMapping("/latest")
    public ResponseEntity<ResumeAnalysis> getLatestAnalysis(
            HttpServletRequest request) {

        String email = (String) request.getAttribute("email");

        return ResponseEntity.ok(
                resumeService.getLatestAnalysis(email)
        );
    }
}