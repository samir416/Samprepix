package com.aiinterview.backend.service.gemini;

import com.aiinterview.backend.dto.gemini.Candidate;
import com.aiinterview.backend.dto.gemini.Content;
import com.aiinterview.backend.dto.gemini.GeminiRequest;
import com.aiinterview.backend.dto.gemini.GeminiResponse;
import com.aiinterview.backend.dto.gemini.Part;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.Duration;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
    public String generateQuestion(String interviewType,
                                   int questionNumber,
                                   int totalQuestions) {

        String prompt = """
                You are an experienced technical interviewer.

                Generate ONLY ONE interview question.

                Interview Type:
                %s

                Current Question:
                %d of %d

                Return only the interview question.
                """
                .formatted(
                        interviewType,
                        questionNumber,
                        totalQuestions
                );

        return callGemini(prompt);
    }

    @Override
    public String evaluateAnswer(String question,
                                 String answer) {

        String prompt = """
                You are an AI interviewer.

                Question:
                %s

                Candidate Answer:
                %s

                Evaluate the answer.

                Give:

                Score: X/10

                Feedback:

                Improvement:
                """
                .formatted(question, answer);

        return callGemini(prompt);
    }

        @Override
    public int extractScore(String evaluation) {

        Matcher matcher = Pattern.compile("(\\d+)\\s*/\\s*10")
                .matcher(evaluation);

        if (matcher.find()) {
            return Integer.parseInt(matcher.group(1));
        }

        matcher = Pattern.compile("(\\d+)").matcher(evaluation);

        if (matcher.find()) {
            return Integer.parseInt(matcher.group(1));
        }

        return 0;
    }

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

        GeminiResponse response = webClient
                .post()
                .uri(apiUrl + "?key=" + apiKey)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .retrieve()
                .bodyToMono(GeminiResponse.class)
                .timeout(Duration.ofSeconds(60))
                .block();

        return extractResponse(response);
    }

        private String extractResponse(GeminiResponse response) {

        if (response == null ||
                response.getCandidates() == null ||
                response.getCandidates().isEmpty()) {

            throw new RuntimeException("No response received from Gemini API.");
        }

        Candidate candidate = response.getCandidates().get(0);

        if (candidate.getContent() == null ||
                candidate.getContent().getParts() == null ||
                candidate.getContent().getParts().isEmpty()) {

            throw new RuntimeException("Invalid Gemini response.");
        }

        Part part = candidate.getContent().getParts().get(0);

        if (part.getText() == null || part.getText().isBlank()) {
            throw new RuntimeException("Gemini returned an empty response.");
        }

        return part.getText().trim();
    }
}
