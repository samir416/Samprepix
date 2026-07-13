package com.aiinterview.backend.service.analyzer;

import com.aiinterview.backend.dto.ai.AIResponse;
import com.aiinterview.backend.service.ai.AIService;
import org.springframework.stereotype.Service;

@Service
public class AIResumeAnalyzerService {

    private final AIService aiService;

    public AIResumeAnalyzerService(AIService aiService) {
        this.aiService = aiService;
    }

    /**
     * Sends extracted resume text to the AI layer
     * and validates the response.
     *
     * @param resumeText extracted resume text
     * @return validated AI response
     */
    public AIResponse analyzeResume(String resumeText) {

        if (resumeText == null || resumeText.isBlank()) {
            throw new IllegalArgumentException("Resume text cannot be empty.");
        }

        try {

            AIResponse response = aiService.analyzeResume(resumeText);

            validateResponse(response);

            return response;

        } catch (Exception exception) {

            throw new RuntimeException(
                    "Failed to analyze resume using AI.",
                    exception
            );
        }
    }

    /**
     * Basic validation for AI response.
     */
    private void validateResponse(AIResponse response) {

        if (response == null) {
            throw new IllegalStateException("AI returned a null response.");
        }

        if (response.getRole() == null || response.getRole().isBlank()) {
            throw new IllegalStateException("AI did not identify a target role.");
        }

        if (response.getAtsScore() < 0 || response.getAtsScore() > 100) {
            throw new IllegalStateException("Invalid ATS score returned by AI.");
        }
    }
}