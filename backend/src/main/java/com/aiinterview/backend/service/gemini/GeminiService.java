package com.aiinterview.backend.service.gemini;

public interface GeminiService {

    String generateQuestion(
            String interviewType,
            int questionNumber,
            int totalQuestions
    );

    String evaluateAnswer(
            String question,
            String answer
    );

    int extractScore(String evaluation);
}