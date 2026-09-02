package com.aiinterview.backend.controller;

import com.aiinterview.backend.dto.ai.AIResponse;
import com.aiinterview.backend.service.ai.AIService;
import org.springframework.http.HttpStatus;
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
    public ResponseEntity<?> testAI() {

        try {

            return ResponseEntity.ok(
                    aiService.analyzeResume(
                            "I am a Java Full Stack Developer. " +
                            "I know Java, Spring Boot, React, MySQL."
                    )
            );

        } catch (Exception exception) {

            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(
                            "AI service failed: "
                                    + getErrorMessage(exception)
                    );
        }
    }

    @PostMapping("/coding-hint")
    public ResponseEntity<?> generateCodingHint(
            @RequestParam String problemTitle,
            @RequestParam String problemDescription,
            @RequestParam String language,
            @RequestParam String code
    ) {

        try {

            if (
                    problemTitle == null ||
                    problemTitle.isBlank()
            ) {
                return ResponseEntity
                        .badRequest()
                        .body("Problem title is required.");
            }

            if (
                    problemDescription == null ||
                    problemDescription.isBlank()
            ) {
                return ResponseEntity
                        .badRequest()
                        .body("Problem description is required.");
            }

            if (
                    language == null ||
                    language.isBlank()
            ) {
                return ResponseEntity
                        .badRequest()
                        .body("Programming language is required.");
            }

            if (code == null) {
                code = "";
            }

            String hint =
                    aiService.generateCodingHint(
                            problemTitle,
                            problemDescription,
                            language,
                            code
                    );

            if (
                    hint == null ||
                    hint.isBlank()
            ) {
                return ResponseEntity
                        .status(
                                HttpStatus.INTERNAL_SERVER_ERROR
                        )
                        .body(
                                "AI service returned an empty hint."
                        );
            }

            return ResponseEntity.ok(hint);

        } catch (IllegalArgumentException exception) {

            return ResponseEntity
                    .badRequest()
                    .body(
                            exception.getMessage()
                    );

        } catch (Exception exception) {

            return ResponseEntity
                    .status(
                            HttpStatus.INTERNAL_SERVER_ERROR
                    )
                    .body(
                            "AI service failed: "
                                    + getErrorMessage(exception)
                    );
        }
    }

    private String getErrorMessage(
            Exception exception
    ) {

        Throwable current =
                exception;

        while (
                current.getCause() != null
        ) {
            current =
                    current.getCause();
        }

        if (
                current.getMessage() != null &&
                !current.getMessage().isBlank()
        ) {
            return current.getMessage();
        }

        return exception.getClass()
                .getSimpleName();
    }
}