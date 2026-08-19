package com.aiinterview.backend.service.coding;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@Service
public class PistonRuntimeService {

    private final WebClient webClient;
    private final ObjectMapper objectMapper;

    @Value("${app.piston.url}")
    private String pistonExecuteUrl;

    public PistonRuntimeService() {
        this.webClient = WebClient.builder().build();
        this.objectMapper = new ObjectMapper();
    }

    public List<PistonRuntime> getRuntimes() {

        String baseUrl = getBaseUrl();

        String response = webClient
                .get()
                .uri(baseUrl + "/api/v2/runtimes")
                .retrieve()
                .bodyToMono(String.class)
                .block(Duration.ofSeconds(15));

        if (response == null || response.isBlank()) {
            return Collections.emptyList();
        }

        try {

            List<Map<String, Object>> data =
                    objectMapper.readValue(
                            response,
                            new TypeReference<List<Map<String, Object>>>() {}
                    );

            List<PistonRuntime> runtimes = new ArrayList<>();

            for (Map<String, Object> item : data) {

                String language =
                        value(item.get("language"));

                String version =
                        value(item.get("version"));

                if (language == null || version == null) {
                    continue;
                }

                List<String> aliases = new ArrayList<>();

                Object aliasValue =
                        item.get("aliases");

                if (aliasValue instanceof List<?> list) {

                    for (Object alias : list) {

                        if (alias != null) {
                            aliases.add(alias.toString());
                        }
                    }
                }

                runtimes.add(
                        new PistonRuntime(
                                language,
                                version,
                                aliases
                        )
                );
            }

            return runtimes;

        } catch (Exception exception) {

            throw new IllegalStateException(
                    "Unable to parse Piston runtimes.",
                    exception
            );
        }
    }

    public PistonRuntime findRuntime(
            String language,
            String version
    ) {

        if (language == null || language.isBlank()) {

            throw new IllegalArgumentException(
                    "Programming language cannot be empty."
            );
        }

        return getRuntimes()
                .stream()
                .filter(runtime ->
                        runtime.matches(
                                language,
                                version
                        )
                )
                .findFirst()
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Piston runtime not available for: "
                                        + language
                        )
                );
    }

    public boolean isRuntimeAvailable(
            String language,
            String version
    ) {

        try {

            findRuntime(
                    language,
                    version
            );

            return true;

        } catch (Exception exception) {

            return false;
        }
    }

    private String getBaseUrl() {

        if (
                pistonExecuteUrl == null ||
                pistonExecuteUrl.isBlank()
        ) {

            throw new IllegalStateException(
                    "app.piston.url is not configured."
            );
        }

        String url =
                pistonExecuteUrl.trim();

        String executePath =
                "/api/v2/execute";

        if (url.endsWith(executePath)) {

            return url.substring(
                    0,
                    url.length() - executePath.length()
            );
        }

        return url.replaceAll("/+$", "");
    }

    private String value(Object value) {

        if (value == null) {
            return null;
        }

        String result =
                value.toString().trim();

        return result.isBlank()
                ? null
                : result;
    }

    public static class PistonRuntime {

        private final String language;
        private final String version;
        private final List<String> aliases;

        public PistonRuntime(
                String language,
                String version,
                List<String> aliases
        ) {

            this.language = language;
            this.version = version;
            this.aliases =
                    aliases == null
                            ? new ArrayList<>()
                            : new ArrayList<>(aliases);
        }

        public String getLanguage() {
            return language;
        }

        public String getVersion() {
            return version;
        }

        public List<String> getAliases() {
            return Collections.unmodifiableList(
                    aliases
            );
        }

        public boolean matches(
                String requestedLanguage,
                String requestedVersion
        ) {

            if (
                    requestedLanguage == null ||
                    requestedLanguage.isBlank()
            ) {

                return false;
            }

            boolean languageMatches =
                    language.equalsIgnoreCase(
                            requestedLanguage.trim()
                    );

            if (!languageMatches) {

                languageMatches =
                        aliases.stream()
                                .anyMatch(alias ->
                                        alias.equalsIgnoreCase(
                                                requestedLanguage.trim()
                                        )
                                );
            }

            if (!languageMatches) {
                return false;
            }

            if (
                    requestedVersion == null ||
                    requestedVersion.isBlank() ||
                    "*".equals(requestedVersion)
            ) {

                return true;
            }

            return version.equalsIgnoreCase(
                    requestedVersion.trim()
            );
        }
    }
}