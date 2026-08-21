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

        validateRepository(repository);
        validateProblemPath(problemPath);

        String normalizedRepository =
                normalizeRepository(repository);

        String normalizedPath =
                normalizePath(problemPath);

        return importSingleProblem(
                normalizedRepository,
                normalizedPath
        );
    }

    public BulkImportResult importRepository(
            String repository
    ) {

        validateRepository(repository);

        String normalizedRepository =
                normalizeRepository(repository);

        List<String> files =
                githubService.getRepositoryFiles(
                        normalizedRepository
                );

        if (files.isEmpty()) {

            return new BulkImportResult(
                    normalizedRepository,
                    0,
                    0,
                    0,
                    List.of()
            );
        }

        List<String> problemPaths =
                extractProblemPaths(files);

        int imported = 0;
        int failed = 0;

        List<String> failedPaths =
                new ArrayList<>();

        for (String problemPath :
                problemPaths) {

            try {

                importSingleProblem(
                        normalizedRepository,
                        problemPath
                );

                imported++;

            } catch (Exception exception) {

                failed++;

                failedPaths.add(
                        problemPath
                );
            }
        }

        return new BulkImportResult(
                normalizedRepository,
                problemPaths.size(),
                imported,
                failed,
                failedPaths
        );
    }

    private CodingProblem importSingleProblem(
            String repository,
            String problemPath
    ) {

        String problemJsonPath =
                problemPath +
                        "/problem.json";

        String testsJsonPath =
                problemPath +
                        "/tests.json";

        String problemJson =
                githubService.getFileContent(
                        repository,
                        problemJsonPath
                );

        if (problemJson.isBlank()) {

            throw new IllegalStateException(
                    "problem.json not found: "
                            + problemJsonPath
            );
        }

        try {

            JsonNode problemNode =
                    objectMapper.readTree(
                            problemJson
                    );

            String title =
                    text(
                            problemNode,
                            "title"
                    );

            if (title.isBlank()) {

                throw new IllegalStateException(
                        "Problem title is required."
                );
            }

            CodingProblem problem =
                    codingProblemRepository
                            .findByTitleIgnoreCase(
                                    title.trim()
                            )
                            .orElseGet(
                                    CodingProblem::new
                            );

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
                    "Unable to import GitHub coding problem: "
                            + problemPath,
                    exception
            );
        }
    }

    private List<String> extractProblemPaths(
            List<String> files
    ) {

        List<String> paths =
                new ArrayList<>();

        for (String file :
                files) {

            if (file == null ||
                    file.isBlank()) {

                continue;
            }

            String normalized =
                    normalizePath(file);

            if (!normalized.endsWith(
                    "/problem.json"
            )) {

                continue;
            }

            String problemPath =
                    normalized.substring(
                            0,
                            normalized.length()
                                    - "/problem.json".length()
                    );

            if (
                    !problemPath.isBlank() &&
                    !paths.contains(problemPath)
            ) {

                paths.add(problemPath);
            }
        }

        return paths;
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
                title.trim()
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

        if (
                configurations.isMissingNode() ||
                configurations.isNull() ||
                !configurations.isObject()
        ) {

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

        if (
                starterCode.isMissingNode() ||
                starterCode.isNull()
        ) {

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

        for (
                CodingTestCase testCase :
                existingTestCases
        ) {

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

            for (
                    JsonNode node :
                    testCasesNode
            ) {

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

                if (
                        input.isBlank() &&
                        expectedOutput.isBlank()
                ) {

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

    private void validateRepository(
            String repository
    ) {

        if (
                repository == null ||
                repository.isBlank()
        ) {

            throw new IllegalArgumentException(
                    "GitHub repository is required."
            );
        }
    }

    private void validateProblemPath(
            String problemPath
    ) {

        if (
                problemPath == null ||
                problemPath.isBlank()
        ) {

            throw new IllegalArgumentException(
                    "Problem path is required."
            );
        }
    }

    private String normalizeRepository(
            String repository
    ) {

        return repository
                .trim()
                .replaceAll(
                        "^https?://github\\.com/",
                        ""
                )
                .replaceAll(
                        "/+$",
                        ""
                );
    }

    private String normalizePath(
            String path
    ) {

        return path
                .trim()
                .replaceAll(
                        "^/+|/+$",
                        ""
                );
    }

    private String text(
            JsonNode node,
            String field
    ) {

        JsonNode value =
                node.path(field);

        if (
                value.isMissingNode() ||
                value.isNull()
        ) {

            return "";
        }

        return value.asText();
    }

    private List<String> stringList(
            JsonNode node
    ) {

        List<String> values =
                new ArrayList<>();

        if (
                node == null ||
                !node.isArray()
        ) {

            return values;
        }

        for (JsonNode item :
                node) {

            if (
                    !item.isNull() &&
                    !item.asText().isBlank()
            ) {

                values.add(
                        item.asText()
                );
            }
        }

        return values;
    }

    public static class BulkImportResult {

        private final String repository;
        private final int discovered;
        private final int imported;
        private final int failed;
        private final List<String> failedPaths;

        public BulkImportResult(
                String repository,
                int discovered,
                int imported,
                int failed,
                List<String> failedPaths
        ) {

            this.repository = repository;
            this.discovered = discovered;
            this.imported = imported;
            this.failed = failed;
            this.failedPaths =
                    failedPaths == null
                            ? List.of()
                            : List.copyOf(
                                    failedPaths
                            );
        }

        public String getRepository() {
            return repository;
        }

        public int getDiscovered() {
            return discovered;
        }

        public int getImported() {
            return imported;
        }

        public int getFailed() {
            return failed;
        }

        public List<String> getFailedPaths() {
            return failedPaths;
        }
    }
}