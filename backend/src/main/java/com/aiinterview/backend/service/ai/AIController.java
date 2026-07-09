package com.aiinterview.backend.controller.ai;

import com.aiinterview.backend.service.ai.AIService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class AIController {

    private final AIService aiService;

    public AIController(AIService aiService) {
        this.aiService = aiService;
    }

    @GetMapping("/ai/test")
    public String testAI() throws Exception {

        return aiService.analyzeResume(
                "I am a Java Full Stack Developer. "
                        + "I know Java, Spring Boot, React, MySQL."
        );
    }
}