package com.aiinterview.backend.service.coding;

import com.aiinterview.backend.entity.CodingProblem;
import com.aiinterview.backend.entity.CodingTestCase;
import com.aiinterview.backend.repository.CodingProblemRepository;
import com.aiinterview.backend.repository.CodingTestCaseRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@Transactional
public class CodingProblemGithubImportService {

    private final CodingProblemGithubService githubService;

    private final CodingProblemRepository codingProblemRepository;

    private final CodingTestCaseRepository codingTestCaseRepository;

    private final ObjectMapper objectMapper;

    public CodingProblemGithubImportService(
            CodingProblemGithubService githubService,
            CodingProblemRepository codingProblemRepository,
            CodingTestCaseRepository codingTestCaseRepository
    ) {
        this.githubService =
                githubService;

        this.codingProblemRepository =
                codingProblemRepository;

        this.codingTestCaseRepository =
                codingTestCaseRepository;

        this.objectMapper =
                new ObjectMapper();
    }

    public CodingProblem importProblem(
            String repository,
            String problemPath
    ) {

        if (repository == null ||
                repository.isBlank()) {

            throw new IllegalArgumentException(
                    "GitHub repository is required."
            );
        }

        if (problemPath == null ||
                problemPath.isBlank()) {

            throw new IllegalArgumentException(
                    "Problem path is required."
            );
        }

        String normalizedPath =
                problemPath
                        .trim()
                        .replaceAll(
                                "^/+|/+$",
                                ""
                        );

        String problemJsonPath =
                normalizedPath +
                        "/problem.json";

        String testsJsonPath =
                normalizedPath +
                        "/tests.json";

        String problemJson =
                githubService.getFileContent(
                        repository,
                        problemJsonPath
                );

        if (problemJson.isBlank()) {

            throw new IllegalStateException(
                    "problem.json not found in GitHub repository."
            );
        }

        try {

            JsonNode problemNode =
                    objectMapper.readTree(
                            problemJson
                    );

            CodingProblem problem =
                    findExistingProblem(
                            text(
                                    problemNode,
                                    "title"
                            )
                    );

            if (problem == null) {

                problem =
                        new CodingProblem();
            }

            updateProblem(
                    problem,
                    problemNode
            );

            CodingProblem savedProblem =
                    codingProblemRepository.save(
                            problem
                    );

            deactivateExistingTestCases(
                    savedProblem
            );

            importTestCases(
                    repository,
                    testsJsonPath,
                    savedProblem
            );

            return savedProblem;

        } catch (Exception exception) {

            throw new IllegalStateException(
                    "Unable to import GitHub coding problem.",
                    exception
            );
        }
    }

    private void updateProblem(
            CodingProblem problem,
            JsonNode node
    ) {

        String title =
                text(
                        node,
                        "title"
                );

        if (title.isBlank()) {

            throw new IllegalStateException(
                    "Problem title is required."
            );
        }

        problem.setTitle(
                title
        );

        problem.setDescription(
                text(
                        node,
                        "description"
                )
        );

        problem.setDifficulty(
                text(
                        node,
                        "difficulty"
                )
                .trim()
                .toUpperCase()
        );

        problem.setTags(
                stringList(
                        node.path("tags")
                )
        );

        problem.setInputExample(
                text(
                        node,
                        "inputExample"
                )
        );

        problem.setOutputExample(
                text(
                        node,
                        "outputExample"
                )
        );

        problem.setConstraints(
                stringList(
                        node.path("constraints")
                )
        );

        problem.setFunctionName(
                text(
                        node,
                        "functionName"
                )
        );

        problem.setFunctionSignature(
                text(
                        node,
                        "functionSignature"
                )
        );

        problem.setReturnType(
                text(
                        node,
                        "returnType"
                )
        );

        problem.setParameterTypes(
                text(
                        node,
                        "parameterTypes"
                )
        );

        problem.setLanguageConfigurations(
                extractLanguageConfigurations(
                        node
                )
        );

        problem.setStarterCode(
                extractDefaultStarterCode(
                        node
                )
        );

        problem.setMinimumExperienceLevel(
                node.path(
                        "minimumExperienceLevel"
                ).asInt(1)
        );

        problem.setActive(true);
    }

    private String extractLanguageConfigurations(
            JsonNode node
    ) {

        JsonNode configurations =
                node.path(
                        "languageConfigurations"
                );

        if (configurations.isMissingNode() ||
                configurations.isNull() ||
                !configurations.isObject()) {

            return "{}";
        }

        try {

            return objectMapper.writeValueAsString(
                    configurations
            );

        } catch (Exception exception) {

            throw new IllegalStateException(
                    "Unable to process language configurations.",
                    exception
            );
        }
    }

    private String extractDefaultStarterCode(
            JsonNode node
    ) {

        JsonNode starterCode =
                node.path(
                        "starterCode"
                );

        if (starterCode.isMissingNode() ||
                starterCode.isNull()) {

            return "";
        }

        if (starterCode.isTextual()) {

            return starterCode.asText();
        }

        return starterCode.toString();
    }

    private void deactivateExistingTestCases(
            CodingProblem problem
    ) {

        List<CodingTestCase> existingTestCases =
                codingTestCaseRepository
                        .findByProblemAndActiveTrueOrderByTestCaseNumberAsc(
                                problem
                        );

        if (existingTestCases.isEmpty()) {

            return;
        }

        for (CodingTestCase testCase :
                existingTestCases) {

            testCase.setActive(false);
        }

        codingTestCaseRepository.saveAll(
                existingTestCases
        );
    }

    private void importTestCases(
            String repository,
            String testsJsonPath,
            CodingProblem problem
    ) {

        String testsJson =
                githubService.getFileContent(
                        repository,
                        testsJsonPath
                );

        if (testsJson.isBlank()) {

            return;
        }

        try {

            JsonNode root =
                    objectMapper.readTree(
                            testsJson
                    );

            JsonNode testCasesNode =
                    root.isArray()
                            ? root
                            : root.path(
                                    "testCases"
                            );

            if (!testCasesNode.isArray()) {

                return;
            }

            List<CodingTestCase> testCases =
                    new ArrayList<>();

            int testCaseNumber = 1;

            for (JsonNode node :
                    testCasesNode) {

                String input =
                        text(
                                node,
                                "input"
                        );

                String expectedOutput =
                        text(
                                node,
                                "expectedOutput"
                        );

                if (input.isBlank() &&
                        expectedOutput.isBlank()) {

                    continue;
                }

                CodingTestCase testCase =
                        CodingTestCase.builder()
                                .problem(problem)
                                .testCaseNumber(
                                        testCaseNumber++
                                )
                                .input(input)
                                .expectedOutput(
                                        expectedOutput
                                )
                                .hidden(
                                        node.path(
                                                "hidden"
                                        ).asBoolean(false)
                                )
                                .active(true)
                                .build();

                testCases.add(
                        testCase
                );
            }

            if (!testCases.isEmpty()) {

                codingTestCaseRepository.saveAll(
                        testCases
                );
            }

        } catch (Exception exception) {

            throw new IllegalStateException(
                    "Unable to import GitHub test cases.",
                    exception
            );
        }
    }

    private CodingProblem findExistingProblem(
            String title
    ) {

        if (title == null ||
                title.isBlank()) {

            return null;
        }

        return codingProblemRepository
                .findAll()
                .stream()
                .filter(
                        problem ->
                                problem.getTitle() != null &&
                                problem.getTitle()
                                        .equalsIgnoreCase(
                                                title.trim()
                                        )
                )
                .findFirst()
                .orElse(null);
    }

    private String text(
            JsonNode node,
            String field
    ) {

        JsonNode value =
                node.path(field);

        if (value.isMissingNode() ||
                value.isNull()) {

            return "";
        }

        return value.asText();
    }

    private List<String> stringList(
            JsonNode node
    ) {

        List<String> values =
                new ArrayList<>();

        if (node == null ||
                !node.isArray()) {

            return values;
        }

        for (JsonNode item :
                node) {

            if (!item.isNull() &&
                    !item.asText().isBlank()) {

                values.add(
                        item.asText()
                );
            }
        }

        return values;
    }
}