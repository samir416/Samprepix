package com.aiinterview.backend.controller;

import com.aiinterview.backend.service.ai.AIService;
import com.aiinterview.backend.service.coding.CodingHintService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/ai")
public class AIController {

    private final AIService aiService;
        private final CodingHintService codingHintService;

        public AIController(
                        AIService aiService,
                        CodingHintService codingHintService
        ) {
        this.aiService = aiService;
                this.codingHintService = codingHintService;
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
            @RequestBody(required = false) Map<String, String> payload,
            @RequestParam(required = false) String problemTitle,
            @RequestParam(required = false) String problemDescription,
            @RequestParam(required = false) String language,
            @RequestParam(required = false) String code
    ) {

        try {

            problemTitle = valueFrom(payload, "problemTitle", problemTitle);
            problemDescription = valueFrom(payload, "problemDescription", problemDescription);
            language = valueFrom(payload, "language", language);
            code = valueFrom(payload, "code", code);

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

            Map<String, Object> hint =
                    codingHintService.generateHint(
                            problemTitle,
                            problemDescription,
                            language,
                            code
                    );
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
                    .body(Map.of(
                            "success", false,
                            "message", "AI Hint is temporarily unavailable."
                    ));
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

    private String valueFrom(
            Map<String, String> payload,
            String key,
            String fallback
    ) {

        if (payload == null || payload.get(key) == null) {
            return fallback;
        }

        return payload.get(key);
    }
}
