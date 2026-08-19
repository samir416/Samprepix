package com.aiinterview.backend.service.ai;

import com.aiinterview.backend.dto.ai.AIResponse;
import com.aiinterview.backend.dto.groq.Choice;
import com.aiinterview.backend.dto.groq.GroqRequest;
import com.aiinterview.backend.dto.groq.GroqResponse;
import com.aiinterview.backend.dto.groq.Message;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

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

    public GroqService() {
        this.webClient = WebClient.create();
        this.objectMapper = new ObjectMapper();
    }

    @Override
    public AIResponse analyzeResume(String resumeText) {

        Message message = new Message(
                "user",
                PromptBuilder.buildResumePrompt(resumeText)
        );

        GroqRequest request = new GroqRequest(
                model,
                List.of(message)
        );

        GroqResponse response = webClient.post()
                .uri(apiUrl)
                .header(
                        HttpHeaders.AUTHORIZATION,
                        "Bearer " + apiKey
                )
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .retrieve()
                .bodyToMono(GroqResponse.class)
                .block(Duration.ofSeconds(30));

        if (response == null
                || response.getChoices() == null
                || response.getChoices().isEmpty()) {

            throw new RuntimeException(
                    "Groq returned an empty response."
            );
        }

        Choice choice =
                response.getChoices().get(0);

        if (choice == null
                || choice.getMessage() == null
                || choice.getMessage().getContent() == null
                || choice.getMessage().getContent().isBlank()) {

            throw new RuntimeException(
                    "Groq returned an empty response."
            );
        }

        String aiContent =
                choice.getMessage().getContent();

        try {

            AIResponse aiResponse =
                    objectMapper.readValue(
                            aiContent,
                            AIResponse.class
                    );

            if (aiResponse.getAtsScore() == null) {
                aiResponse.setAtsScore(0);
            }

            return aiResponse;

        } catch (Exception exception) {

            throw new RuntimeException(
                    "Groq returned an invalid JSON response.",
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

        Message message = new Message(
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
                webClient.post()
                        .uri(apiUrl)
                        .header(
                                HttpHeaders.AUTHORIZATION,
                                "Bearer " + apiKey
                        )
                        .contentType(
                                MediaType.APPLICATION_JSON
                        )
                        .bodyValue(request)
                        .retrieve()
                        .bodyToMono(
                                GroqResponse.class
                        )
                        .block(
                                Duration.ofSeconds(30)
                        );

        if (response == null
                || response.getChoices() == null
                || response.getChoices().isEmpty()) {

            throw new RuntimeException(
                    "Groq returned an empty coding hint."
            );
        }

        Choice choice =
                response.getChoices().get(0);

        if (choice == null
                || choice.getMessage() == null
                || choice.getMessage().getContent() == null
                || choice.getMessage()
                        .getContent()
                        .isBlank()) {

            throw new RuntimeException(
                    "Groq returned an empty coding hint."
            );
        }

        return choice.getMessage()
                .getContent()
                .trim();
    }
}