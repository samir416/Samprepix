package com.aiinterview.backend.controller;

import com.aiinterview.backend.dto.ai.AIResponse;
import com.aiinterview.backend.service.ai.AIService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/ai")
public class AIController {

    private final AIService aiService;

    public AIController(AIService aiService) {
        this.aiService = aiService;
    }

    @GetMapping("/test")
    public AIResponse testAI() throws Exception {
        return aiService.analyzeResume(
                "I am a Java Full Stack Developer. " +
                "I know Java, Spring Boot, React, MySQL."
        );
    }

    @PostMapping("/coding-hint")
    public ResponseEntity<String> generateCodingHint(
            @RequestParam String problemTitle,
            @RequestParam String problemDescription,
            @RequestParam String language,
            @RequestParam String code
    ) throws Exception {

        String hint = aiService.generateCodingHint(
                problemTitle,
                problemDescription,
                language,
                code
        );

        return ResponseEntity.ok(hint);
    }
}