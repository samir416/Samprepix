package com.aiinterview.backend.service.gemini;

import java.util.List;

public interface GeminiService {

    String generateQuestion(

        String targetRole,

        List<String> skills,

        String experienceLevel,

        List<String> previousQuestions,

        List<String> previousAnswers,

        List<String> weakAreas,

        List<String> strongAreas

);

    String evaluateAnswer(

        String targetRole,

        String experienceLevel,

        List<String> skills,

        String question,

        String candidateAnswer,

        List<String> previousQuestions,

        List<String> previousAnswers,

        String idealAnswer

);

    List<String> generateSkillSuggestions(

            String role,

            String query

    );

}