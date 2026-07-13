package com.aiinterview.backend.controller;

import com.aiinterview.backend.entity.ResumeAnalysis;
import com.aiinterview.backend.model.ResumeResponse;
import com.aiinterview.backend.service.ResumeServiceV2;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/resume")
public class ResumeController {

    private final ResumeServiceV2 resumeService;

    public ResumeController(ResumeServiceV2 resumeService) {
        this.resumeService = resumeService;
    }

    /**
     * Analyze Resume
     */
    @PostMapping("/analyze")
    public ResponseEntity<ResumeResponse> analyzeResume(
            @RequestParam("file") MultipartFile file,
            HttpServletRequest request) throws Exception {

        String email = (String) request.getAttribute("email");

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