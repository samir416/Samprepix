package com.aiinterview.backend.service.coding;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Production-quality AI Coding Hint service powered by Groq's free tier.
 * Includes strict quota protection: in-memory caching, per-user cooldowns,
 * daily rate limits, code truncation, and automatic fallback.
 */
@Service
public class CodingHintService {

    private static final Logger log = LoggerFactory.getLogger(CodingHintService.class);

    private final WebClient webClient;
    private final ObjectMapper objectMapper;

    @Value("${groq.api.url:https://api.groq.com/openai/v1/chat/completions}")
    private String groqApiUrl;

    @Value("${groq.api.key:}")
    private String groqApiKey;

    @Value("${groq.coding-hint.model:openai/gpt-oss-120b}")
    private String primaryModel;

    private static final int MAX_CODE_CHARS = 1500;
    private static final int MAX_HINT_TOKENS = 250;
    private static final int COOLDOWN_SECONDS = 6;
    private static final int DAILY_QUOTA_PER_USER = 60;
    private static final Duration CACHE_TTL = Duration.ofMinutes(15);

    // In-memory hint cache: key -> CachedHint
    private final Map<String, CachedHint> hintCache = new ConcurrentHashMap<>();

    // Per-user cooldown tracking: userKey -> last request timestamp
    private final Map<String, Instant> userLastRequest = new ConcurrentHashMap<>();

    // Per-user daily request count: userKey -> count
    private final Map<String, UserDailyQuota> userDailyQuota = new ConcurrentHashMap<>();

    public CodingHintService(
            WebClient.Builder webClientBuilder,
            ObjectMapper objectMapper
    ) {
        this.webClient = webClientBuilder.build();
        this.objectMapper = objectMapper;
    }

    private record CachedHint(String hint, Instant createdAt, int level, String model) {}
    private record UserDailyQuota(AtomicInteger count, Instant resetAt) {}

    public String getPrimaryModel() {
        return primaryModel;
    }

    public void clearCache() {
        hintCache.clear();
        userLastRequest.clear();
        userDailyQuota.clear();
    }

    public int getCacheSize() {
        return hintCache.size();
    }

    public boolean isCached(String problemTitle, String language, int level) {
        String cacheKey = (problemTitle.trim() + ":" + language.trim() + ":" + level).toLowerCase();
        return hintCache.containsKey(cacheKey);
    }

    public Map<String, Object> generateHint(
            String problemTitle,
            String problemDescription,
            String language,
            String code
    ) {
        return generateHint(problemTitle, problemDescription, language, code, 1, "anonymous");
    }

    public Map<String, Object> generateHint(
            String problemTitle,
            String problemDescription,
            String language,
            String code,
            int level,
            String userKey
    ) {
        if (problemTitle == null || problemTitle.isBlank()) {
            throw new IllegalArgumentException("Problem title is required.");
        }
        if (problemDescription == null || problemDescription.isBlank()) {
            throw new IllegalArgumentException("Problem description is required.");
        }
        if (language == null || language.isBlank()) {
            language = "Java";
        }

        int hintLevel = Math.min(Math.max(1, level), 4);
        String safeUser = (userKey == null || userKey.isBlank()) ? "anonymous" : userKey.trim();

        // 1. Check in-memory cache first (prevents redundant AI API calls)
        String cacheKey = (problemTitle.trim() + ":" + language.trim() + ":" + hintLevel).toLowerCase();
        CachedHint cached = hintCache.get(cacheKey);
        if (cached != null && Instant.now().isBefore(cached.createdAt().plus(CACHE_TTL))) {
            return buildResponse(cached.hint(), hintLevel, cached.model(), true);
        }

        // 2. Cooldown check per user
        Instant now = Instant.now();
        Instant lastRequest = userLastRequest.get(safeUser);
        if (lastRequest != null && Duration.between(lastRequest, now).toSeconds() < COOLDOWN_SECONDS) {
            long remaining = COOLDOWN_SECONDS - Duration.between(lastRequest, now).toSeconds();
            // If cached version exists, return it, else report cooldown
            if (cached != null) {
                return buildResponse(cached.hint(), hintLevel, cached.model(), true);
            }
            throw new IllegalStateException("Please wait " + remaining + " second(s) before requesting another hint.");
        }

        // 3. Daily quota check per user
        UserDailyQuota quota = userDailyQuota.compute(safeUser, (k, existing) -> {
            if (existing == null || now.isAfter(existing.resetAt())) {
                return new UserDailyQuota(new AtomicInteger(1), now.plus(Duration.ofDays(1)));
            }
            existing.count().incrementAndGet();
            return existing;
        });

        if (quota.count().get() > DAILY_QUOTA_PER_USER) {
            if (cached != null) {
                return buildResponse(cached.hint(), hintLevel, cached.model(), true);
            }
            throw new IllegalStateException("Daily AI Hint limit reached. Please try again tomorrow.");
        }

        userLastRequest.put(safeUser, now);

        // 4. Validate API Key
        if (groqApiKey == null || groqApiKey.isBlank()) {
            log.warn("GROQ_API_KEY is not configured.");
            throw new IllegalStateException("AI Hint is temporarily unavailable. Please configure GROQ_API_KEY.");
        }

        // 5. Construct progressive, token-efficient prompt
        String truncatedCode = truncateCode(code);
        String systemInstruction = buildSystemInstruction(problemTitle, language, hintLevel);
        String userContent = buildUserContent(problemTitle, problemDescription, language, truncatedCode);

        // 6. Call Groq with primary model (zero paid fallback; fail gracefully on error)
        try {
            String hint = callGroq(primaryModel, systemInstruction, userContent);
            hintCache.put(cacheKey, new CachedHint(hint, now, hintLevel, primaryModel));
            return buildResponse(hint, hintLevel, primaryModel, false);
        } catch (WebClientResponseException ex) {
            log.warn("Groq request failed with HTTP {}: {}",
                    ex.getStatusCode(), ex.getResponseBodyAsString());

            if (ex.getStatusCode().value() == 429) {
                throw new IllegalStateException("AI Hint is temporarily unavailable due to high demand. Please try again later.");
            }
            throw new IllegalStateException("AI Hint is temporarily unavailable. Please try again later.");
        } catch (Exception ex) {
            log.error("Groq AI Hint invocation error: {}", ex.getMessage());
            throw new IllegalStateException("AI Hint is temporarily unavailable. Please try again later.");
        }
    }

    private String callGroq(String model, String systemPrompt, String userPrompt) throws Exception {
        Map<String, Object> requestBody = new LinkedHashMap<>();
        requestBody.put("model", model);
        requestBody.put("messages", List.of(
                Map.of("role", "system", "content", systemPrompt),
                Map.of("role", "user", "content", userPrompt)
        ));
        requestBody.put("max_tokens", MAX_HINT_TOKENS);
        requestBody.put("temperature", 0.4);

        String response = webClient.post()
                .uri(groqApiUrl)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + groqApiKey.trim())
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(requestBody)
                .retrieve()
                .bodyToMono(String.class)
                .block(Duration.ofSeconds(12));

        if (response == null || response.isBlank()) {
            throw new IllegalStateException("Groq returned an empty response.");
        }

        JsonNode root = objectMapper.readTree(response);
        JsonNode choices = root.path("choices");
        if (choices.isArray() && !choices.isEmpty()) {
            String content = choices.get(0).path("message").path("content").asText("").trim();
            if (!content.isBlank()) {
                return cleanHintOutput(content);
            }
        }
        throw new IllegalStateException("No content found in Groq response.");
    }

    private String cleanHintOutput(String text) {
        // Remove markdown wrappers or thinking blocks if present
        String cleaned = text.replaceAll("(?s)<think>.*?</think>", "").trim();
        if (cleaned.startsWith("```") && cleaned.endsWith("```")) {
            cleaned = cleaned.replaceAll("^```[a-zA-Z]*\\n", "").replaceAll("\\n```$", "").trim();
        }
        return cleaned;
    }

    private String truncateCode(String code) {
        if (code == null || code.isBlank()) {
            return "";
        }
        String trimmed = code.trim();
        if (trimmed.length() <= MAX_CODE_CHARS) {
            return trimmed;
        }
        return trimmed.substring(0, MAX_CODE_CHARS) + "\n... [code truncated for brevity]";
    }

    private String buildSystemInstruction(String problemTitle, String language, int level) {
        String levelGuide = switch (level) {
            case 1 -> "Level 1 (Concept): Provide a gentle conceptual direction (1-3 sentences). Give intuition and guide thinking without revealing specific algorithms or code.";
            case 2 -> "Level 2 (Observation): Share a key mathematical, structural, or logical observation or invariant about the problem. Do not reveal full solution.";
            case 3 -> "Level 3 (Algorithm): Suggest the most fitting algorithmic technique or data structure (e.g., Two Pointers, Hash Table, Sliding Window) for " + language + ". Mention language-specific collection considerations if helpful, but do not write out complete code.";
            case 4 -> "Level 4 (Implementation & Edge Cases): Highlight 1-2 subtle edge cases, boundary conditions, or potential pitfalls in " + language + ". Keep it concise.";
            default -> "Provide a helpful, progressive coding hint without giving away the full solution.";
        };

        return "You are an expert, encouraging coding interview mentor.\n" +
                "Rule: NEVER write out the complete solution code or give away the answer.\n" +
                "Rule: Keep your response concise (under 80 words) and directly actionable.\n" +
                levelGuide;
    }

    private String buildUserContent(String title, String description, String language, String code) {
        StringBuilder sb = new StringBuilder();
        sb.append("Problem: ").append(title).append("\n");
        sb.append("Description: ").append(description.length() > 500 ? description.substring(0, 500) + "..." : description).append("\n");
        sb.append("Language: ").append(language).append("\n");
        if (!code.isBlank()) {
            sb.append("Current Code:\n").append(code).append("\n");
        }
        return sb.toString();
    }

    private Map<String, Object> buildResponse(String hint, int level, String model, boolean cached) {
        String levelName = switch (level) {
            case 1 -> "Concept";
            case 2 -> "Observation";
            case 3 -> "Algorithm";
            case 4 -> "Edge Cases";
            default -> "General";
        };

        Map<String, Object> res = new LinkedHashMap<>();
        res.put("success", true);
        res.put("hint", hint);
        res.put("level", level);
        res.put("levelName", levelName);
        res.put("provider", "groq");
        res.put("model", model);
        res.put("cached", cached);
        return res;
    }
}
