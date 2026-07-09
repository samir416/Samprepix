package com.aiinterview.backend.service.ai;

import com.aiinterview.backend.dto.groq.Choice;
import com.aiinterview.backend.dto.groq.GroqRequest;
import com.aiinterview.backend.dto.groq.GroqResponse;
import com.aiinterview.backend.dto.groq.Message;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;

@Service
public class GroqService implements AIService {

    private final WebClient webClient;

    @Value("${groq.api.url}")
    private String apiUrl;

    @Value("${groq.api.key}")
    private String apiKey;

    @Value("${groq.api.model}")
    private String model;

    public GroqService(WebClient.Builder builder) {

        this.webClient = builder.build();
    }

    @Override
    public String analyzeResume(String resumeText) {

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

                        .block();

        if (response == null
                || response.getChoices() == null
                || response.getChoices().isEmpty()) {

            return "No response from AI.";
        }

        Choice choice =
                response.getChoices().get(0);

        return choice
                .getMessage()
                .getContent();
    }
}