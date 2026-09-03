package com.aiinterview.backend.service.coding;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.Map;

@Service
public class CodingHintService {

    private final WebClient webClient;
    private final ObjectMapper objectMapper;

    @Value("${coding.hint.ollama.url:http://localhost:11434}")
    private String ollamaUrl;

    @Value("${coding.hint.ollama.model:mistral:latest}")
    private String ollamaModel;

    public CodingHintService(
            WebClient.Builder webClientBuilder,
            ObjectMapper objectMapper
    ) {
        this.webClient = webClientBuilder.build();
        this.objectMapper = objectMapper;
    }

    public Map<String, Object> generateHint(
            String problemTitle,
            String problemDescription,
            String language,
            String code
    ) {
        if (problemTitle == null || problemTitle.isBlank() ||
                problemDescription == null || problemDescription.isBlank() ||
                language == null || language.isBlank()) {
            throw new IllegalArgumentException("Problem context is required.");
        }

        String prompt = "Give one concise coding hint, without providing the complete solution.\n" +
                "Problem: " + problemTitle + "\n" +
                "Description: " + problemDescription + "\n" +
                "Language: " + language + "\n" +
                "Current code:\n" + (code == null ? "" : code);

        Map<String, Object> request = new LinkedHashMap<>();
        request.put("model", ollamaModel);
        request.put("prompt", prompt);
        request.put("stream", false);

        try {
            String response = webClient.post()
                    .uri(ollamaUrl.trim().replaceAll("/+$", "") + "/api/generate")
                    .bodyValue(request)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block(Duration.ofSeconds(45));

            JsonNode root = objectMapper.readTree(response);
            String hint = root.path("response").asText("").trim();

            if (hint.isBlank()) {
                throw new IllegalStateException("Coding hint provider returned an empty response.");
            }

            return Map.of(
                    "success", true,
                    "hint", hint,
                    "provider", "ollama"
            );
        } catch (IllegalArgumentException exception) {
            throw exception;
        } catch (Exception exception) {
            throw new IllegalStateException(
                    "AI Hint is temporarily unavailable. Start Ollama with the configured coding model.",
                    exception
            );
        }
    }
}
