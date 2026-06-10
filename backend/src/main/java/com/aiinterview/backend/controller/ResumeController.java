package com.aiinterview.backend.controller;

import com.aiinterview.backend.model.ResumeResponse;
import com.aiinterview.backend.service.ResumeService;

import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/resume")
public class ResumeController {

    private final ResumeService resumeService;

    public ResumeController(
            ResumeService resumeService
    ) {
        this.resumeService = resumeService;
    }

    @GetMapping("/analyze")
    public ResumeResponse analyzeResume() {

        return resumeService.analyzeResume();
    }
}