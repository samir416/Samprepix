package com.aiinterview.backend.service.coding;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@Service
public class PistonRuntimeService {

    private static final Duration REQUEST_TIMEOUT =
            Duration.ofSeconds(15);

    private final WebClient webClient;
    private final ObjectMapper objectMapper;

    @Value("${app.piston.url}")
    private String pistonExecuteUrl;

    public PistonRuntimeService(
            WebClient.Builder webClientBuilder,
            ObjectMapper objectMapper
    ) {
        this.webClient =
                webClientBuilder.build();

        this.objectMapper =
                objectMapper;
    }

    public List<PistonRuntime> getRuntimes() {

        String baseUrl =
                getBaseUrl();

        String response;

        try {
            response =
                    webClient
                            .get()
                            .uri(
                                    baseUrl +
                                            "/api/v2/runtimes"
                            )
                            .retrieve()
                            .bodyToMono(String.class)
                            .block(
                                    REQUEST_TIMEOUT
                            );
        } catch (Exception exception) {
            return Collections.emptyList();
        }

        if (
                response == null ||
                response.isBlank()
        ) {
            return Collections.emptyList();
        }

        try {

            List<Map<String, Object>> data =
                    objectMapper.readValue(
                            response,
                            new TypeReference<
                                    List<Map<String, Object>>
                                    >() {
                                    }
                    );

            if (
                    data == null ||
                    data.isEmpty()
            ) {
                return Collections.emptyList();
            }

            Map<String, PistonRuntime> uniqueRuntimes =
                    new LinkedHashMap<>();

            for (
                    Map<String, Object> item :
                    data
            ) {

                if (item == null) {
                    continue;
                }

                String language =
                        value(
                                item.get("language")
                        );

                String version =
                        value(
                                item.get("version")
                        );

                if (
                        language == null ||
                        version == null
                ) {
                    continue;
                }

                List<String> aliases =
                        readAliases(
                                item.get("aliases")
                        );

                PistonRuntime runtime =
                        new PistonRuntime(
                                language,
                                version,
                                aliases
                        );

                String key =
                        canonicalLanguage(language) +
                                "|" +
                                normalize(version);

                uniqueRuntimes.putIfAbsent(
                        key,
                        runtime
                );
            }

            return new ArrayList<>(
                    uniqueRuntimes.values()
            );

        } catch (Exception exception) {

            throw new IllegalStateException(
                    "Unable to parse Piston runtimes.",
                    exception
            );
        }
    }

    public List<PistonRuntime> getAvailableLanguages() {

        List<PistonRuntime> runtimes =
                getRuntimes();

        if (runtimes.isEmpty()) {
            return Collections.emptyList();
        }

        Map<String, PistonRuntime> languages =
                new LinkedHashMap<>();

        for (
                PistonRuntime runtime :
                runtimes
        ) {

            String key =
                    canonicalLanguage(
                            runtime.getLanguage()
                    );

            if (key.isBlank()) {
                continue;
            }

            languages.putIfAbsent(
                    key,
                    runtime
            );
        }

        return new ArrayList<>(
                languages.values()
        );
    }

    public PistonRuntime findRuntime(
            String language,
            String version
    ) {

        if (
                language == null ||
                language.isBlank()
        ) {
            throw new IllegalArgumentException(
                    "Programming language cannot be empty."
            );
        }

        List<PistonRuntime> runtimes =
                getRuntimes();

        if (runtimes.isEmpty()) {
            throw new IllegalStateException(
                    "No Piston runtimes are currently available."
            );
        }

        String requestedLanguage =
                canonicalLanguage(
                        language
                );

        String requestedVersion =
                version == null
                        ? ""
                        : version.trim();

        if (
                requestedLanguage.isBlank()
        ) {
            throw new IllegalArgumentException(
                    "Programming language is invalid."
            );
        }

        if (
                !requestedVersion.isBlank() &&
                !"*".equals(
                        requestedVersion
                )
        ) {

            for (
                    PistonRuntime runtime :
                    runtimes
            ) {

                if (
                        runtime.matches(
                                requestedLanguage,
                                requestedVersion
                        )
                ) {
                    return runtime;
                }
            }
        }

        for (
                PistonRuntime runtime :
                runtimes
        ) {

            if (
                    runtime.matches(
                            requestedLanguage,
                            null
                    )
            ) {
                return runtime;
            }
        }

        throw new IllegalArgumentException(
                "Piston runtime not available for: "
                        + language
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

    private List<String> readAliases(
            Object aliasValue
    ) {

        if (
                !(aliasValue instanceof List<?> list)
        ) {
            return new ArrayList<>();
        }

        List<String> aliases =
                new ArrayList<>();

        for (
                Object alias :
                list
        ) {

            String value =
                    value(alias);

            if (
                    value != null &&
                    !aliases.contains(value)
            ) {
                aliases.add(value);
            }
        }

        return aliases;
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
                pistonExecuteUrl
                        .trim()
                        .replaceAll(
                                "/+$",
                                ""
                        );

        String executePath =
                "/api/v2/execute";

        if (
                url.endsWith(
                        executePath
                )
        ) {
            return url.substring(
                    0,
                    url.length()
                            - executePath.length()
            );
        }

        return url;
    }

    private String value(
            Object value
    ) {

        if (value == null) {
            return null;
        }

        String result =
                value.toString().trim();

        return result.isBlank()
                ? null
                : result;
    }

    private String normalize(
            String value
    ) {

        if (
                value == null ||
                value.isBlank()
        ) {
            return "";
        }

        return value
                .trim()
                .toLowerCase(
                        Locale.ROOT
                );
    }

    private String canonicalLanguage(
            String language
    ) {

        String normalized =
                normalize(language);

        return switch (normalized) {

            case "js",
                 "node",
                 "nodejs" ->
                    "javascript";

            case "ts" ->
                    "typescript";

            case "golang" ->
                    "go";

            case "cpp",
                 "cxx" ->
                    "c++";

            case "csharp",
                 "cs" ->
                    "c#";

            case "py",
                 "python3" ->
                    "python";

            case "rs" ->
                    "rust";

            case "kt" ->
                    "kotlin";

            case "sh",
                 "shell" ->
                    "bash";

            default ->
                    normalized;
        };
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

            this.language =
                    language;

            this.version =
                    version;

            this.aliases =
                    aliases == null
                            ? new ArrayList<>()
                            : new ArrayList<>(
                                    aliases
                            );
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

        public boolean matchesLanguage(
                String requestedLanguage
        ) {

            if (
                    requestedLanguage == null ||
                    requestedLanguage.isBlank()
            ) {
                return false;
            }

            String requested =
                    canonicalize(
                            requestedLanguage
                    );

            if (
                    canonicalize(language)
                            .equals(requested)
            ) {
                return true;
            }

            return aliases.stream()
                    .filter(
                            alias ->
                                    alias != null &&
                                    !alias.isBlank()
                    )
                    .anyMatch(
                            alias ->
                                    canonicalize(alias)
                                            .equals(requested)
                    );
        }

        public boolean matchesVersion(
                String requestedVersion
        ) {

            if (
                    requestedVersion == null ||
                    requestedVersion.isBlank() ||
                    "*".equals(
                            requestedVersion.trim()
                    )
            ) {
                return true;
            }

            return version != null &&
                    version
                            .trim()
                            .equalsIgnoreCase(
                                    requestedVersion.trim()
                            );
        }

        public boolean matches(
                String requestedLanguage,
                String requestedVersion
        ) {

            return matchesLanguage(
                    requestedLanguage
            ) &&
                    matchesVersion(
                            requestedVersion
                    );
        }

        private static String canonicalize(
                String language
        ) {

            if (
                    language == null ||
                    language.isBlank()
            ) {
                return "";
            }

            String normalized =
                    language
                            .trim()
                            .toLowerCase(
                                    Locale.ROOT
                            );

            return switch (normalized) {

                case "js",
                     "node",
                     "nodejs" ->
                    "javascript";

                case "ts" ->
                    "typescript";

                case "golang" ->
                    "go";

                case "cpp",
                     "cxx" ->
                    "c++";

                case "csharp",
                     "cs" ->
                    "c#";

                case "py",
                     "python3" ->
                    "python";

                case "rs" ->
                    "rust";

                case "kt" ->
                    "kotlin";

                case "sh",
                     "shell" ->
                    "bash";

                default ->
                    normalized;
            };
        }
    }
}