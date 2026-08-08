package com.aiinterview.backend.service.gemini;

import com.aiinterview.backend.dto.ai.SkillSuggestionResponse;
import com.aiinterview.backend.dto.gemini.Candidate;
import com.aiinterview.backend.dto.gemini.Content;
import com.aiinterview.backend.dto.gemini.GeminiRequest;
import com.aiinterview.backend.dto.gemini.GeminiResponse;
import com.aiinterview.backend.dto.gemini.Part;
import com.aiinterview.backend.service.ai.PromptBuilder;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.Duration;
import java.util.List;
import com.fasterxml.jackson.databind.ObjectMapper;

@Service
public class GeminiServiceImpl implements GeminiService {

    private final WebClient webClient;

    @Value("${gemini.api.url}")
    private String apiUrl;

    @Value("${gemini.api.key}")
    private String apiKey;

    public GeminiServiceImpl(WebClient webClient) {
        this.webClient = webClient;
    }

   @Override
public String generateQuestion(

        String targetRole,

        List<String> skills,

        String experienceLevel,

        List<String> previousQuestions,

        List<String> previousAnswers,

        List<String> weakAreas,

        List<String> strongAreas

) {

    String prompt = """
You are a Senior Technical Interviewer.

Conduct the complete interview only in English.

Ask ONLY ONE interview theory question.

Do not greet the candidate.

Do not explain anything.

Do not answer your own question.

Do not provide hints.

Candidate Profile

Target Role:
%s

Experience Level:
%s

Selected Skills:
%s

Weak Areas:
%s

Strong Areas:
%s

Previously Asked Questions:
%s

Previous Candidate Answers:
%s

Interview Rules

1. Ask exactly ONE interview question.

2. Never repeat any previous question.

3. Use ONLY the selected technical skills.

4. Never ask questions from technologies outside the selected skills.

5. If multiple skills exist, balance them naturally.

6. Prefer weak areas before strong areas.

7. Gradually increase the difficulty.

8. Ask interview theory questions only.

9. Never ask coding questions.

10. Mix conceptual, practical, real-world and scenario-based questions.

11. Ask natural follow-up questions whenever appropriate.

12. Avoid asking the same concept twice.

13. Never mention previous scores, evaluation or feedback.

14. Keep the question concise.

15. Return ONLY the interview question.

""".formatted(

            targetRole,

            experienceLevel,

            skills.isEmpty()

                    ? "No skills selected"

                    : String.join(", ", skills),

            weakAreas.isEmpty()

                    ? "No weak areas identified"

                    : String.join(", ", weakAreas),

            strongAreas.isEmpty()

                    ? "No strong areas identified"

                    : String.join(", ", strongAreas),

            previousQuestions.isEmpty()

                    ? "No previous questions"

                    : String.join("\n", previousQuestions),

            previousAnswers.isEmpty()

                    ? "No previous answers"

                    : String.join("\n", previousAnswers)

    );

    return callGemini(prompt);

}



   @Override
public String evaluateAnswer(

        String targetRole,

        String experienceLevel,

        List<String> skills,

        String question,

        String candidateAnswer,

        List<String> previousQuestions,

        List<String> previousAnswers,

        String idealAnswer

) {

    String prompt = """
You are a Senior Technical Interviewer.

Evaluate the candidate's answer professionally.

The interview language is English.

Candidate Profile

Target Role:
%s

Experience Level:
%s

Selected Skills:
%s

Current Question:
%s

Candidate Answer:
%s

Previous Questions:
%s

Previous Answer Summary:
%s

Reference Ideal Answer:
%s

Evaluation Rules

1. Evaluate ONLY the current answer.

2. Ignore previous scores.

3. Do not compare with previous answers unless required.

4. Evaluate semantic meaning instead of exact wording.

5. Accept different technically correct explanations.

6. Ignore grammar mistakes if technical meaning is correct.

7. If the answer is empty, score 0.

8. If the candidate says "I don't know", score fairly.

9. Never hallucinate information.

10. Evaluate only what the candidate actually answered.

11. Use ONLY the selected technical skills.

12. Be strict but fair.

Return ONLY valid JSON.

{
  "technicalAccuracy": 0,
  "completeness": 0,
  "communication": 0,
  "overallScore": 0,
  "performance": "",
  "idealAnswer": "",
  "feedback": "",
  "nextFocusSkill": "",
  "difficulty": "",
  "strengths": [],
  "missingConcepts": []
}

Rules for JSON:

- technicalAccuracy = 0-100
- completeness = 0-100
- communication = 0-100
- overallScore = 0-100
- performance = Outstanding | Excellent | Good | Average | Needs Improvement
- difficulty = Easy | Medium | Hard

Return JSON only.

Do not use markdown.

Do not explain anything.

""".formatted(

            targetRole,

            experienceLevel,

            skills.isEmpty()

                    ? "No skills selected"

                    : String.join(", ", skills),

            question,

            candidateAnswer == null || candidateAnswer.isBlank()

                    ? "No Answer"

                    : candidateAnswer,

            previousQuestions.isEmpty()

                    ? "No previous questions"

                    : String.join("\n", previousQuestions),

            previousAnswers.isEmpty()

                    ? "No previous answers"

                    : String.join("\n", previousAnswers),

            idealAnswer == null || idealAnswer.isBlank()

                    ? "Not Available"

                    : idealAnswer

    );

    return callGemini(prompt);

}


   @Override
public List<String> generateSkillSuggestions(

        String role,

        String query

) {

    String prompt = PromptBuilder.buildSkillSuggestionPrompt(

            role,

            query

    );

    String response = callGemini(prompt);

    return parseSkillSuggestions(response);

}

private static final ObjectMapper OBJECT_MAPPER =

        new ObjectMapper();

private String callGemini(String prompt) {

    GeminiRequest request = new GeminiRequest(

            List.of(

                    new Content(

                            List.of(

                                    new Part(prompt)

                            )

                    )

            )

    );

    try {

        GeminiResponse response = webClient

                .post()

                .uri(apiUrl + "?key=" + apiKey)

                .contentType(MediaType.APPLICATION_JSON)

                .accept(MediaType.APPLICATION_JSON)

                .bodyValue(request)

                .retrieve()

                .bodyToMono(GeminiResponse.class)

                .timeout(Duration.ofSeconds(60))

                .block();

        return extractResponse(response);

    }

    catch (org.springframework.web.reactive.function.client.WebClientResponseException exception) {

        throw new RuntimeException(

                "Gemini API Error : "

                        + exception.getStatusCode().value()

                        + " | "

                        + exception.getResponseBodyAsString()

        );

    }

    catch (Exception exception) {

        throw new RuntimeException(

                "Failed to communicate with Gemini API.",

                exception

        );

    }

}

       

 
private List<String> parseSkillSuggestions(

        String json

) {

    try {

        SkillSuggestionResponse response =

                OBJECT_MAPPER.readValue(

                        json,

                        SkillSuggestionResponse.class

                );

        if (response == null || response.getSkills() == null) {

            return List.of();

        }

        return response.getSkills();

    }

    catch (Exception exception) {

        return List.of();

    }

}

      private String extractResponse(

        GeminiResponse response

) {

    if (

            response == null ||

            response.getCandidates() == null ||

            response.getCandidates().isEmpty()

    ) {

        throw new RuntimeException(

                "Gemini returned an empty response."

        );

    }

    Candidate candidate = response.getCandidates().get(0);

    if (

            candidate == null ||

            candidate.getContent() == null ||

            candidate.getContent().getParts() == null ||

            candidate.getContent().getParts().isEmpty()

    ) {

        throw new RuntimeException(

                "Invalid Gemini response."

        );

    }

    Part part = candidate

            .getContent()

            .getParts()

            .get(0);

    if (

            part == null ||

            part.getText() == null ||

            part.getText().isBlank()

    ) {

        throw new RuntimeException(

                "Gemini returned an empty text response."

        );

    }

    return part.getText().trim();

}

}
