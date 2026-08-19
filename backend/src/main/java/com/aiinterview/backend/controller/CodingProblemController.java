package com.aiinterview.backend.controller;

import com.aiinterview.backend.dto.coding.CodingProblemResponse;
import com.aiinterview.backend.dto.coding.CodingPublicTestCaseResponse;
import com.aiinterview.backend.entity.CodingProblem;
import com.aiinterview.backend.entity.CodingTestCase;
import com.aiinterview.backend.entity.User;
import com.aiinterview.backend.repository.CodingTestCaseRepository;
import com.aiinterview.backend.repository.UserRepository;
import com.aiinterview.backend.service.coding.CodingProblemService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/coding/problems")
public class CodingProblemController {

    private final CodingProblemService codingProblemService;
    private final CodingTestCaseRepository codingTestCaseRepository;
    private final UserRepository userRepository;
    private final ObjectMapper objectMapper;

    public CodingProblemController(
            CodingProblemService codingProblemService,
            CodingTestCaseRepository codingTestCaseRepository,
            UserRepository userRepository
    ) {
        this.codingProblemService = codingProblemService;
        this.codingTestCaseRepository = codingTestCaseRepository;
        this.userRepository = userRepository;
        this.objectMapper = new ObjectMapper();
    }

    @GetMapping
    public ResponseEntity<List<CodingProblemResponse>> getProblems(
            Authentication authentication
    ) {

        User user = getAuthenticatedUser(authentication);

        Integer experienceLevel = getExperienceLevel(user);

        List<CodingProblem> problems =
                codingProblemService.getProblemsForExperience(
                        experienceLevel
                );

        return ResponseEntity.ok(
                problems.stream()
                        .map(this::toResponse)
                        .toList()
        );
    }

    @GetMapping("/{problemId}")
    public ResponseEntity<CodingProblemResponse> getProblem(
            @PathVariable Long problemId
    ) {

        CodingProblem problem =
                codingProblemService.getProblemById(problemId);

        return ResponseEntity.ok(
                toResponse(problem)
        );
    }

    @GetMapping("/difficulty/{difficulty}")
    public ResponseEntity<List<CodingProblemResponse>> getProblemsByDifficulty(
            @PathVariable String difficulty,
            Authentication authentication
    ) {

        User user = getAuthenticatedUser(authentication);

        Integer experienceLevel = getExperienceLevel(user);

        List<CodingProblem> problems =
                codingProblemService.getProblemsForExperience(
                        experienceLevel
                );

        List<CodingProblemResponse> filteredProblems =
                problems.stream()
                        .filter(problem ->
                                problem.getDifficulty() != null &&
                                problem.getDifficulty()
                                        .equalsIgnoreCase(difficulty)
                        )
                        .map(this::toResponse)
                        .toList();

        return ResponseEntity.ok(filteredProblems);
    }

    private CodingProblemResponse toResponse(
            CodingProblem problem
    ) {

        List<CodingTestCase> testCases =
                codingTestCaseRepository
                        .findByProblemAndHiddenFalseAndActiveTrueOrderByTestCaseNumberAsc(
                                problem
                        );

        List<CodingPublicTestCaseResponse> publicTestCases =
                testCases.stream()
                        .map(testCase ->
                                CodingPublicTestCaseResponse
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
                                        .build()
                        )
                        .toList();

        Map<String, Object> languageConfigurations =
                parseLanguageConfigurations(
                        problem.getLanguageConfigurations()
                );

        Map<String, String> starterCodes =
                parseStarterCodes(
                        problem.getStarterCode(),
                        languageConfigurations
                );

        return CodingProblemResponse.builder()
                .id(problem.getId())
                .title(problem.getTitle())
                .description(problem.getDescription())
                .difficulty(problem.getDifficulty())
                .tags(problem.getTags())
                .inputExample(problem.getInputExample())
                .outputExample(problem.getOutputExample())
                .constraints(problem.getConstraints())
                .minimumExperienceLevel(
                        problem.getMinimumExperienceLevel()
                )
                .active(problem.isActive())
                .starterCodes(starterCodes)
                .languageConfigurations(languageConfigurations)
                .testCases(publicTestCases)
                .build();
    }

    private Map<String, String> parseStarterCodes(
            String starterCode,
            Map<String, Object> languageConfigurations
    ) {

        Map<String, String> starterCodes =
                new LinkedHashMap<>();

        if (languageConfigurations != null &&
                !languageConfigurations.isEmpty()) {

            for (Map.Entry<String, Object> entry :
                    languageConfigurations.entrySet()) {

                String language = entry.getKey();

                if (language == null ||
                        language.isBlank()) {
                    continue;
                }

                if (!(entry.getValue() instanceof Map<?, ?> config)) {
                    continue;
                }

                Object starter = config.get("starterCode");

                if (starter == null) {
                    starter = config.get("starter_code");
                }

                if (starter != null &&
                        !starter.toString().isBlank()) {

                    starterCodes.put(
                            language.trim().toLowerCase(),
                            starter.toString()
                    );
                }
            }
        }

        if (!starterCodes.isEmpty()) {
            return starterCodes;
        }

        if (starterCode == null ||
                starterCode.isBlank()) {

            return starterCodes;
        }

        String[] sections =
                starterCode.split(
                        "\\r?\\n\\r?\\n"
                );

        for (String section : sections) {

            if (section == null ||
                    section.isBlank()) {
                continue;
            }

            int separator =
                    section.indexOf(":\n");

            if (separator < 0) {
                separator =
                        section.indexOf(":\r\n");
            }

            if (separator <= 0) {
                continue;
            }

            String language =
                    section.substring(
                            0,
                            separator
                    )
                    .trim()
                    .toLowerCase();

            String code =
                    section.substring(
                            separator + 2
                    );

            if (!language.isBlank() &&
                    !code.isBlank()) {

                starterCodes.put(
                        language,
                        code
                );
            }
        }

        return starterCodes;
    }

    private Map<String, Object> parseLanguageConfigurations(
            String configurations
    ) {

        if (configurations == null ||
                configurations.isBlank()) {

            return new LinkedHashMap<>();
        }

        try {

            Map<String, Object> parsed =
                    objectMapper.readValue(
                            configurations,
                            new TypeReference<Map<String, Object>>() {
                            }
                    );

            if (parsed == null) {
                return new LinkedHashMap<>();
            }

            return new LinkedHashMap<>(parsed);

        } catch (Exception exception) {

            return new LinkedHashMap<>();
        }
    }

    private Integer getExperienceLevel(
            User user
    ) {

        if (user.getProfile() == null ||
                user.getProfile()
                        .getExperienceLevel() == null ||
                user.getProfile()
                        .getExperienceLevel()
                        .isBlank()) {

            return 1;
        }

        String experience =
                user.getProfile()
                        .getExperienceLevel()
                        .trim()
                        .toUpperCase();

        return switch (experience) {

            case "BEGINNER" ->
                    1;

            case "INTERMEDIATE" ->
                    2;

            case "ADVANCED" ->
                    3;

            default ->
                    1;
        };
    }

    private User getAuthenticatedUser(
            Authentication authentication
    ) {

        if (authentication == null ||
                authentication.getName() == null ||
                authentication.getName().isBlank()) {

            throw new IllegalStateException(
                    "Authenticated user not found."
            );
        }

        return userRepository
                .findByEmail(
                        authentication.getName()
                )
                .orElseThrow(
                        () ->
                                new IllegalStateException(
                                        "User account not found."
                                )
                );
    }
}