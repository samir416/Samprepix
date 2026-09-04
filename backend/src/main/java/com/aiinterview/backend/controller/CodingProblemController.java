package com.aiinterview.backend.controller;

import com.aiinterview.backend.config.CentralLanguageRegistry;
import com.aiinterview.backend.dto.coding.CodingProblemResponse;
import com.aiinterview.backend.dto.coding.CodingPublicTestCaseResponse;
import com.aiinterview.backend.dto.coding.CodingProblemListResponse;
import com.aiinterview.backend.entity.CodingProblem;
import com.aiinterview.backend.entity.CodingTestCase;
import com.aiinterview.backend.repository.CodingTestCaseRepository;
import com.aiinterview.backend.service.coding.CodingProblemService;
import com.aiinterview.backend.service.coding.PistonRuntimeService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

@RestController
@RequestMapping("/api/coding/problems")
public class CodingProblemController {

    private final CodingProblemService codingProblemService;
    private final CodingTestCaseRepository codingTestCaseRepository;
    private final PistonRuntimeService pistonRuntimeService;
    private final ObjectMapper objectMapper;

    public CodingProblemController(
            CodingProblemService codingProblemService,
            CodingTestCaseRepository codingTestCaseRepository,
            PistonRuntimeService pistonRuntimeService,
            ObjectMapper objectMapper
    ) {
        this.codingProblemService =
                codingProblemService;

        this.codingTestCaseRepository =
                codingTestCaseRepository;

        this.pistonRuntimeService =
                pistonRuntimeService;

        this.objectMapper =
                objectMapper;
    }

    @GetMapping
    public ResponseEntity<Page<CodingProblemListResponse>> getProblems(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String difficulty,
            @RequestParam(required = false) String tag,
            @RequestParam(required = false) String category
    ) {
        int safePage = Math.max(0, page);
        int safeSize = Math.min(Math.max(1, size), 100);
        PageRequest pageable = PageRequest.of(
                safePage,
                safeSize,
                Sort.by(Sort.Direction.ASC, "id")
        );

        return ResponseEntity.ok(
                codingProblemService
                        .searchProblems(search, difficulty, tag, category, pageable)
                        .map(this::toListResponse)
        );
    }

    public ResponseEntity<Page<CodingProblemListResponse>> getProblems(
            int page,
            int size,
            String search,
            String difficulty,
            String tag
    ) {
        return getProblems(page, size, search, difficulty, tag, null);
    }

    @GetMapping("/tags")
    public ResponseEntity<List<String>> getAvailableTags() {
        return ResponseEntity.ok(
                codingProblemService.getAvailableTags()
        );
    }

    @GetMapping("/languages")
    public ResponseEntity<Map<String, Object>> getAvailableLanguages() {
        return ResponseEntity.ok(Map.of(
                "all", CentralLanguageRegistry.getAllLanguages(),
                "popular", CentralLanguageRegistry.getPopularLanguages(),
                "more", CentralLanguageRegistry.getMoreLanguages(),
                "database", CentralLanguageRegistry.getDatabaseLanguages(),
                "programming", CentralLanguageRegistry.getProgrammingLanguages()
        ));
    }

    private CodingProblemListResponse toListResponse(CodingProblem problem) {
        return CodingProblemListResponse.builder()
                .id(problem.getId())
                .title(problem.getTitle())
                .difficulty(problem.getDifficulty())
                .category(problem.getCategory() == null ? "DSA" : problem.getCategory())
                .tags(problem.getTags() == null
                        ? List.of()
                        : List.copyOf(problem.getTags()))
                .minimumExperienceLevel(problem.getMinimumExperienceLevel())
                .active(problem.isActive())
                .build();
    }

    @GetMapping("/difficulty/{difficulty}")
    public ResponseEntity<List<CodingProblemResponse>>
    getProblemsByDifficulty(
            @PathVariable String difficulty
    ) {

        return ResponseEntity.ok(
                codingProblemService
                        .getProblemsByDifficulty(
                                difficulty
                        )
                        .stream()
                        .map(this::toResponse)
                        .toList()
        );
    }

    @GetMapping("/experience/{experienceLevel}")
    public ResponseEntity<List<CodingProblemResponse>>
    getProblemsForExperience(
            @PathVariable Integer experienceLevel
    ) {

        return ResponseEntity.ok(
                codingProblemService
                        .getProblemsForExperience(
                                experienceLevel
                        )
                        .stream()
                        .map(this::toResponse)
                        .toList()
        );
    }

    @GetMapping("/{problemId}")
    public ResponseEntity<CodingProblemResponse> getProblem(
            @PathVariable Long problemId
    ) {

        CodingProblem problem =
                codingProblemService.getProblemById(
                        problemId
                );

        return ResponseEntity.ok(
                toResponse(problem)
        );
    }

    private CodingProblemResponse toResponse(
            CodingProblem problem
    ) {

        List<CodingTestCase> testCases =
                codingTestCaseRepository
                        .findByProblemAndHiddenFalseAndActiveTrueOrderByTestCaseNumberAsc(
                                problem
                        );

        List<CodingPublicTestCaseResponse>
                publicTestCases =
                testCases
                        .stream()
                        .map(this::toPublicTestCaseResponse)
                        .toList();

        Map<String, Object>
                languageConfigurations =
                parseLanguageConfigurations(
                        problem.getLanguageConfigurations()
                );

        Map<String, Object>
                executableConfigurations =
                filterAvailableLanguageConfigurations(
                        languageConfigurations
                );

        Map<String, String>
                starterCodes =
                parseStarterCodes(
                        problem.getStarterCode(),
                        executableConfigurations
                );

        return CodingProblemResponse
                .builder()
                .id(problem.getId())
                .title(problem.getTitle())
                .description(problem.getDescription())
                .difficulty(problem.getDifficulty())
                .category(problem.getCategory() == null ? "DSA" : problem.getCategory())
                .tags(
                        problem.getTags() == null
                                ? List.of()
                                : List.copyOf(
                                        problem.getTags()
                                )
                )
                .inputExample(problem.getInputExample())
                .outputExample(problem.getOutputExample())
                .constraints(
                        problem.getConstraints() == null
                                ? List.of()
                                : List.copyOf(
                                        problem.getConstraints()
                                )
                )
                .minimumExperienceLevel(
                        problem.getMinimumExperienceLevel()
                )
                .active(problem.isActive())
                .starterCodes(starterCodes)
                .languageConfigurations(
                        executableConfigurations
                )
                .testCases(publicTestCases)
                .build();
    }

    private CodingPublicTestCaseResponse
    toPublicTestCaseResponse(
            CodingTestCase testCase
    ) {

        return CodingPublicTestCaseResponse
                .builder()
                .testCaseNumber(
                        testCase.getTestCaseNumber()
                )
                .input(
                        testCase.getInput()
                )
                .expectedOutput(
                        testCase.getExpectedOutput()
                )
                .build();
    }

    private Map<String, Object>
    filterAvailableLanguageConfigurations(
            Map<String, Object> configurations
    ) {

        Map<String, Object> result =
                new LinkedHashMap<>();

        if (
                configurations == null ||
                configurations.isEmpty()
        ) {
            return result;
        }

                List<PistonRuntimeService.PistonRuntime>
                                runtimes;

                try {
                        runtimes = pistonRuntimeService.getRuntimes();
                } catch (Exception exception) {
                        return configurations;
                }

        if (
                runtimes == null ||
                runtimes.isEmpty()
        ) {
            return result;
        }

        for (
                Map.Entry<String, Object> entry :
                configurations.entrySet()
        ) {

            String configurationKey =
                    normalizeLanguageKey(
                            entry.getKey()
                    );

            if (
                    configurationKey.isBlank()
            ) {
                continue;
            }

            if (
                    !(entry.getValue()
                            instanceof Map<?, ?> rawMap)
            ) {
                continue;
            }

            Map<String, Object> configuration =
                    copyConfiguration(
                            rawMap
                    );

            String runtimeLanguage =
                    readConfigurationValue(
                            configuration,
                            "runtimeLanguage"
                    );

            if (
                    runtimeLanguage == null ||
                    runtimeLanguage.isBlank()
            ) {
                runtimeLanguage =
                        configurationKey;
            }

            String requestedVersion =
                    readConfigurationValue(
                            configuration,
                            "runtimeVersion"
                    );

            if ("mysql".equalsIgnoreCase(configurationKey) || "sql".equalsIgnoreCase(configurationKey)) {
                configuration.put("runtimeLanguage", "mysql");
                configuration.put("runtimeVersion", "8.0");
                ensureDisplayMetadata(configuration, "mysql");
                result.put(configurationKey, configuration);
                continue;
            }

            PistonRuntimeService.PistonRuntime runtime;

            try {

                runtime =
                        pistonRuntimeService.findRuntime(
                                runtimeLanguage,
                                requestedVersion
                        );

            } catch (Exception exception) {
                continue;
            }

            if (runtime == null) {
                continue;
            }

            configuration.put(
                    "runtimeLanguage",
                    runtime.getLanguage()
            );

            configuration.put(
                    "runtimeVersion",
                    runtime.getVersion()
            );

            ensureDisplayMetadata(
                    configuration,
                    runtime.getLanguage()
            );

            result.put(
                    configurationKey,
                    configuration
            );
        }

        return result;
    }

    private Map<String, Object>
    copyConfiguration(
            Map<?, ?> rawMap
    ) {

        Map<String, Object> configuration =
                new LinkedHashMap<>();

        for (
                Map.Entry<?, ?> entry :
                rawMap.entrySet()
        ) {

            if (
                    entry.getKey() == null
            ) {
                continue;
            }

            configuration.put(
                    entry.getKey().toString(),
                    entry.getValue()
            );
        }

        return configuration;
    }

    private void ensureDisplayMetadata(
            Map<String, Object> configuration,
            String runtimeLanguage
    ) {

        if (
                !configuration.containsKey(
                        "displayName"
                ) ||
                isBlankValue(
                        configuration.get(
                                "displayName"
                        )
                )
        ) {

            configuration.put(
                    "displayName",
                    buildDisplayName(
                            runtimeLanguage
                    )
            );
        }

        if (
                !configuration.containsKey(
                        "monacoLanguage"
                ) ||
                isBlankValue(
                        configuration.get(
                                "monacoLanguage"
                        )
                )
        ) {

            configuration.put(
                    "monacoLanguage",
                    getMonacoLanguage(
                            runtimeLanguage
                    )
            );
        }
    }

    private String buildDisplayName(
            String language
    ) {

        if (
                language == null ||
                language.isBlank()
        ) {
            return "Language";
        }

        String normalized =
                language
                        .trim()
                        .toLowerCase(
                                Locale.ROOT
                        );

        return switch (normalized) {

            case "java" ->
                    "Java";

            case "python", "python3" ->
                    "Python";

            case "javascript", "js", "node", "nodejs" ->
                    "JavaScript";

            case "typescript", "ts" ->
                    "TypeScript";

            case "kotlin" ->
                    "Kotlin";

            case "go", "golang" ->
                    "Go";

            case "rust", "rs" ->
                    "Rust";

            case "c" ->
                    "C";

            case "c++", "cpp", "cxx" ->
                    "C++";

            case "c#", "csharp", "cs" ->
                    "C#";

            case "php" ->
                    "PHP";

            case "ruby" ->
                    "Ruby";

            case "swift" ->
                    "Swift";

            case "dart" ->
                    "Dart";

            case "scala" ->
                    "Scala";

            case "bash", "shell", "sh" ->
                    "Bash";

            case "mysql", "sql" ->
                    "MySQL";

            default ->
                    Character.toUpperCase(
                            language.charAt(0)
                    ) +
                    language.substring(1);
        };
    }

    private String getMonacoLanguage(
            String language
    ) {

        if (
                language == null ||
                language.isBlank()
        ) {
            return "plaintext";
        }

        String normalized =
                language
                        .trim()
                        .toLowerCase(
                                Locale.ROOT
                        );

        return switch (normalized) {

            case "cpp", "c++", "cxx" ->
                    "cpp";

            case "csharp", "c#", "cs" ->
                    "csharp";

            case "javascript", "js", "node", "nodejs" ->
                    "javascript";

            case "typescript", "ts" ->
                    "typescript";

            case "golang" ->
                    "go";

            case "bash", "shell", "sh" ->
                    "shell";

            case "python3", "py" ->
                    "python";

            case "rs" ->
                    "rust";

            case "kt" ->
                    "kotlin";

            case "mysql", "sql" ->
                    "sql";

            default ->
                    normalized;
        };
    }

    private String readConfigurationValue(
            Map<String, Object> configuration,
            String key
    ) {

        if (
                configuration == null ||
                key == null ||
                key.isBlank()
        ) {
            return null;
        }

        Object value =
                configuration.get(key);

        if (value == null) {
            return null;
        }

        String result =
                value.toString().trim();

        return result.isBlank()
                ? null
                : result;
    }

    private boolean isBlankValue(
            Object value
    ) {

        return value == null ||
                value.toString()
                        .trim()
                        .isBlank();
    }

    private String normalizeLanguageKey(
            String language
    ) {

        if (
                language == null ||
                language.isBlank()
        ) {
            return "";
        }

        return language
                .trim()
                .toLowerCase(
                        Locale.ROOT
                );
    }

    private Map<String, String>
    parseStarterCodes(
            String starterCode,
            Map<String, Object> languageConfigurations
    ) {

        Map<String, String> starterCodes =
                new LinkedHashMap<>();

        if (
                languageConfigurations != null &&
                !languageConfigurations.isEmpty()
        ) {

            for (
                    Map.Entry<String, Object> entry :
                    languageConfigurations.entrySet()
            ) {

                String language =
                        normalizeLanguageKey(
                                entry.getKey()
                        );

                if (
                        language.isBlank()
                ) {
                    continue;
                }

                if (
                        !(entry.getValue()
                                instanceof Map<?, ?> config)
                ) {
                    continue;
                }

                Object starter =
                        config.get(
                                "starterCode"
                        );

                if (starter == null) {

                    starter =
                            config.get(
                                    "starter_code"
                            );
                }

                if (
                        starter != null &&
                        !starter.toString()
                                .isBlank()
                ) {

                    starterCodes.put(
                            language,
                            starter.toString()
                    );
                }
            }
        }

        if (
                !starterCodes.isEmpty()
        ) {
            return starterCodes;
        }

        if (
                starterCode == null ||
                starterCode.isBlank()
        ) {
            return starterCodes;
        }

        try {

            Map<String, String> parsed =
                    objectMapper.readValue(
                            starterCode,
                            new TypeReference<
                                    Map<String, String>
                            >() {
                            }
                    );

            if (
                    parsed != null &&
                    !parsed.isEmpty()
            ) {

                for (
                        Map.Entry<String, String> entry :
                        parsed.entrySet()
                ) {

                    String language =
                            normalizeLanguageKey(
                                    entry.getKey()
                            );

                    String code =
                            entry.getValue();

                    if (
                            !language.isBlank() &&
                            code != null &&
                            !code.isBlank()
                    ) {

                        starterCodes.put(
                                language,
                                code
                        );
                    }
                }
            }

        } catch (Exception ignored) {
        }

        return starterCodes;
    }

    private Map<String, Object>
    parseLanguageConfigurations(
            String configurations
    ) {

        if (
                configurations == null ||
                configurations.isBlank()
        ) {
            return new LinkedHashMap<>();
        }

        try {

            Map<String, Object> parsed =
                    objectMapper.readValue(
                            configurations,
                            new TypeReference<
                                    Map<String, Object>
                            >() {
                            }
                    );

            if (
                    parsed == null ||
                    parsed.isEmpty()
            ) {
                return new LinkedHashMap<>();
            }

            return new LinkedHashMap<>(
                    parsed
            );

        } catch (Exception exception) {

            return new LinkedHashMap<>();
        }
    }
}
