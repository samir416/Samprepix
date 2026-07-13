package com.aiinterview.backend.service.ai;

public class PromptBuilder {

    private PromptBuilder() {
    }

    public static String buildResumePrompt(String resumeText) {

        return """
You are an expert ATS (Applicant Tracking System) Resume Analyzer.

Your task is to analyze the resume professionally as a senior technical recruiter.

Rules:

1. Return ONLY valid JSON.
2. Do NOT return Markdown.
3. Do NOT return explanations.
4. Do NOT return extra text.
5. All fields must always be present.
6. ATS Score must be between 0 and 100.
7. detectedSkills, missingSkills, strengths, weaknesses and suggestions must always be arrays.
8. Determine the most suitable job role from the resume.
9. Suggestions must be practical and specific.

ATS Score Guidelines:

90-100 = Excellent resume
75-89 = Good resume with minor improvements
60-74 = Average resume
40-59 = Needs significant improvements
0-39 = Poor resume

Return JSON exactly in this format:

{
  "role": "",
  "atsScore": 0,
  "detectedSkills": [],
  "missingSkills": [],
  "strengths": [],
  "weaknesses": [],
  "suggestions": []
}

Resume:

""" + resumeText;
    }

    /**
     * Generates the next interview question.
     */
    public static String buildInterviewQuestionPrompt(
            String interviewType,
            int questionNumber,
            int totalQuestions
    ) {

        return """
You are an expert technical interviewer.

Generate ONLY ONE interview question.

Rules:
1. Do NOT provide the answer.
2. Keep the question clear.
3. Difficulty should increase gradually.
4. Return plain text only.
5. Do not use Markdown.

Interview Type:
""" + interviewType + """

Question Number:
""" + questionNumber + " of " + totalQuestions;
    }

    /**
     * Evaluates the candidate's answer.
     */
    public static String buildAnswerEvaluationPrompt(
            String question,
            String answer
    ) {

        return """
You are an experienced technical interviewer.

Evaluate the candidate's answer.

Return ONLY valid JSON.

Format:

{
  "score": 0,
  "feedback": "",
  "strengths": [],
  "improvements": []
}

Question:
""" + question + """

Answer:
""" + answer;
    }
}