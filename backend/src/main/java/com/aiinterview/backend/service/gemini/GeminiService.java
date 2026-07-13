package com.aiinterview.backend.service.gemini;

public interface GeminiService {

    /**
     * Generates the next interview question.
     */
    String generateQuestion(
            String interviewType,
            int questionNumber,
            int totalQuestions
    );

    /**
     * Evaluates the candidate answer.
     */
    String evaluateAnswer(
            String question,
            String answer
    );
}