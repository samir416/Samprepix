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

    public static String buildSkillSuggestionPrompt(
            String role,
            String query
    ) {

        return """
You are a senior software architect, technical interviewer and hiring manager.

Your task is to suggest technical skills only.

Rules:

1. Return ONLY valid JSON.
2. Never return Markdown.
3. Never return explanations.
4. Never return numbering.
5. Maximum 10 skills.
6. Ignore unrelated technologies.
7. Skills must match the target role.
8. Predict incomplete user input.

Examples

Role:
Java Full Stack Developer

Search:
spr

Result

{
"skills":[
"Spring Boot",
"Spring Security",
"Spring MVC",
"Spring Data JPA",
"Spring Cloud"
]
}

Role:
React Developer

Search:
rea

Result

{
"skills":[
"React",
"React Router",
"React Query",
"React Hook Form"
]
}

Now generate response.

Role:

""" + role + """

User Search:

""" + query;
    }

    public static String buildCodingHintPrompt(
            String problemTitle,
            String problemDescription,
            String language,
            String code
    ) {

        return """
You are an expert coding interview mentor.

Give the candidate a useful hint for solving the coding problem.

Rules:

1. Return plain text only.
2. Do not use Markdown.
3. Do not provide the complete solution.
4. Do not provide complete code.
5. Do not directly reveal the final answer.
6. Explain the next useful idea or approach.
7. Point out a likely mistake if the submitted code has one.
8. Keep the hint concise and practical.
9. The hint must be relevant to the selected programming language.

Problem:
""" + problemTitle + """

Description:
""" + problemDescription + """

Language:
""" + language + """

Current Code:
""" + code + """

Give one helpful hint now.
""";
    }
}