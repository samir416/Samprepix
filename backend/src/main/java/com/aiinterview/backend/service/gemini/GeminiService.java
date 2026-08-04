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

    java.util.List<String> generateSkillSuggestions(

        String role,

        String query

);
}