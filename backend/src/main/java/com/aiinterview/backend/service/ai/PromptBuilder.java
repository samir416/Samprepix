package com.aiinterview.backend.service.ai;

public class PromptBuilder {

    private PromptBuilder() {
    }

    public static String buildResumePrompt(String resumeText) {

        return """
Analyze the following resume.

Return ONLY valid JSON.

Include:

role
atsScore
detectedSkills
missingSkills
strengths
weaknesses
suggestions

Resume:

""" + resumeText;
    }
}