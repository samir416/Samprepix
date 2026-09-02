package com.aiinterview.backend.service.ai;

import com.aiinterview.backend.dto.ai.AIResponse;
import com.aiinterview.backend.dto.groq.Choice;
import com.aiinterview.backend.dto.groq.GroqRequest;
import com.aiinterview.backend.dto.groq.GroqResponse;
import com.aiinterview.backend.dto.groq.Message;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.time.Duration;
import java.util.List;

@Service
public class GroqService implements AIService {

    private final WebClient webClient;
    private final ObjectMapper objectMapper;

    @Value("${groq.api.url}")
    private String apiUrl;

    @Value("${groq.api.key}")
    private String apiKey;

    @Value("${groq.api.model}")
    private String model;

    public GroqService(
            ObjectMapper objectMapper
    ) {
        this.webClient = WebClient.create();
        this.objectMapper = objectMapper;
    }

    @Override
    public AIResponse analyzeResume(
            String resumeText
    ) {

        validateConfiguration();

        Message message =
                new Message(
                        "user",
                        PromptBuilder.buildResumePrompt(
                                resumeText
                        )
                );

        GroqRequest request =
                new GroqRequest(
                        model,
                        List.of(message)
                );

        GroqResponse response =
                executeRequest(
                        request
                );

        Choice choice =
                extractChoice(
                        response,
                        "Groq returned an empty response."
                );

        String aiContent =
                choice.getMessage()
                        .getContent();

        try {

            AIResponse aiResponse =
                    objectMapper.readValue(
                            aiContent,
                            AIResponse.class
                    );

            if (
                    aiResponse.getAtsScore()
                            == null
            ) {
                aiResponse.setAtsScore(0);
            }

            return aiResponse;

        } catch (Exception exception) {

            throw new RuntimeException(
                    "Groq returned an invalid JSON response: "
                            + aiContent,
                    exception
            );
        }
    }

    @Override
    public String generateCodingHint(
            String problemTitle,
            String problemDescription,
            String language,
            String code
    ) {

        validateConfiguration();

        if (
                problemTitle == null ||
                problemTitle.isBlank()
        ) {
            throw new IllegalArgumentException(
                    "Problem title is required."
            );
        }

        if (
                problemDescription == null ||
                problemDescription.isBlank()
        ) {
            throw new IllegalArgumentException(
                    "Problem description is required."
            );
        }

        if (
                language == null ||
                language.isBlank()
        ) {
            throw new IllegalArgumentException(
                    "Programming language is required."
            );
        }

        if (
                code == null
        ) {
            code = "";
        }

        Message message =
                new Message(
                        "user",
                        PromptBuilder.buildCodingHintPrompt(
                                problemTitle,
                                problemDescription,
                                language,
                                code
                        )
                );

        GroqRequest request =
                new GroqRequest(
                        model,
                        List.of(message)
                );

        GroqResponse response =
                executeRequest(
                        request
                );

        Choice choice =
                extractChoice(
                        response,
                        "Groq returned an empty coding hint."
                );

        return choice.getMessage()
                .getContent()
                .trim();
    }

    private GroqResponse executeRequest(
            GroqRequest request
    ) {

        try {

            return webClient.post()
                    .uri(apiUrl)
                    .header(
                            HttpHeaders.AUTHORIZATION,
                            "Bearer " + apiKey
                    )
                    .contentType(
                            MediaType.APPLICATION_JSON
                    )
                    .accept(
                            MediaType.APPLICATION_JSON
                    )
                    .bodyValue(request)
                    .retrieve()
                    .onStatus(
                            HttpStatusCode::isError,
                            response ->
                                    response.bodyToMono(
                                            String.class
                                    ).map(
                                            errorBody ->
                                                    new RuntimeException(
                                                            buildGroqErrorMessage(
                                                                    response.statusCode(),
                                                                    errorBody
                                                            )
                                                    )
                                    )
                    )
                    .bodyToMono(
                            GroqResponse.class
                    )
                    .block(
                            Duration.ofSeconds(30)
                    );

        } catch (
                WebClientResponseException exception
        ) {

            throw new RuntimeException(
                    buildGroqErrorMessage(
                            exception.getStatusCode(),
                            exception.getResponseBodyAsString()
                    ),
                    exception
            );

        } catch (
                RuntimeException exception
        ) {

            throw exception;

        } catch (
                Exception exception
        ) {

            throw new RuntimeException(
                    "Unable to connect to Groq API: "
                            + exception.getMessage(),
                    exception
            );
        }
    }

    private Choice extractChoice(
            GroqResponse response,
            String emptyMessage
    ) {

        if (
                response == null ||
                response.getChoices() == null ||
                response.getChoices().isEmpty()
        ) {
            throw new RuntimeException(
                    emptyMessage
            );
        }

        Choice choice =
                response.getChoices()
                        .get(0);

        if (
                choice == null ||
                choice.getMessage() == null ||
                choice.getMessage()
                        .getContent() == null ||
                choice.getMessage()
                        .getContent()
                        .isBlank()
        ) {
            throw new RuntimeException(
                    emptyMessage
            );
        }

        return choice;
    }

    private void validateConfiguration() {

        if (
                apiUrl == null ||
                apiUrl.isBlank()
        ) {
            throw new IllegalStateException(
                    "Groq API URL is not configured."
            );
        }

        if (
                apiKey == null ||
                apiKey.isBlank() ||
                apiKey.equals(
                        "${GROQ_API_KEY}"
                )
        ) {
            throw new IllegalStateException(
                    "Groq API key is not configured. Set GROQ_API_KEY."
            );
        }

        if (
                model == null ||
                model.isBlank()
        ) {
            throw new IllegalStateException(
                    "Groq API model is not configured."
            );
        }
    }

    private String buildGroqErrorMessage(
            HttpStatusCode status,
            String errorBody
    ) {

        String body =
                errorBody == null
                        ? ""
                        : errorBody.trim();

        if (body.length() > 1000) {
            body =
                    body.substring(
                            0,
                            1000
                    );
        }

        return "Groq API request failed. HTTP "
                + status.value()
                + ". Response: "
                + body;
    }
}