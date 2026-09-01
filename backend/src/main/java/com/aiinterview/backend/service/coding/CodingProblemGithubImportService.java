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
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

@Service
public class CodingProblemGithubImportService {

    private final CodingProblemGithubService githubService;
    private final CodingProblemRepository codingProblemRepository;
    private final CodingTestCaseRepository codingTestCaseRepository;
    private final ObjectMapper objectMapper;

    public CodingProblemGithubImportService(
            CodingProblemGithubService githubService,
            CodingProblemRepository codingProblemRepository,
            CodingTestCaseRepository codingTestCaseRepository,
            ObjectMapper objectMapper
    ) {
        this.githubService = githubService;
        this.codingProblemRepository = codingProblemRepository;
        this.codingTestCaseRepository = codingTestCaseRepository;
        this.objectMapper = objectMapper;
    }

    @Transactional
    public CodingProblem importProblem(
            String repository,
            String problemPath
    ) {

        validateRepository(repository);
        validateProblemPath(problemPath);

        return importSingleProblem(
                normalizeRepository(repository),
                normalizeProblemPath(problemPath)
        );
    }

    @Transactional
    public List<CodingProblem> importAllProblems(
            String repository
    ) {

        validateRepository(repository);

        String normalizedRepository =
                normalizeRepository(repository);

        List<String> files =
                githubService.getRepositoryFiles(
                        normalizedRepository
                );

        if (
                files == null ||
                files.isEmpty()
        ) {
            return List.of();
        }

        List<String> problemPaths =
                extractProblemPaths(files);

        List<CodingProblem> problems =
                new ArrayList<>();

        for (
                String problemPath :
                problemPaths
        ) {

            try {

                CodingProblem problem =
                        importSingleProblem(
                                normalizedRepository,
                                problemPath
                        );

                if (problem != null) {
                    problems.add(problem);
                }

            } catch (Exception ignored) {
            }
        }

        return problems;
    }

    @Transactional
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

        if (
                files == null ||
                files.isEmpty()
        ) {

            return new BulkImportResult(
                    normalizedRepository,
                    0,
                    0,
                    0,
                    List.of(),
                    List.of()
            );
        }

        List<String> problemPaths =
                extractProblemPaths(files);

        int discovered =
                problemPaths.size();

        int imported = 0;
        int failed = 0;

        List<String> importedPaths =
                new ArrayList<>();

        List<String> failedPaths =
                new ArrayList<>();

        for (
                String problemPath :
                problemPaths
        ) {

            try {

                importSingleProblem(
                        normalizedRepository,
                        problemPath
                );

                imported++;

                importedPaths.add(
                        problemPath
                );

            } catch (Exception exception) {

                failed++;

                failedPaths.add(
                        problemPath
                );
            }
        }

        return new BulkImportResult(
                normalizedRepository,
                discovered,
                imported,
                failed,
                failedPaths,
                importedPaths
        );
    }

    private CodingProblem importSingleProblem(
            String repository,
            String problemPath
    ) {

        String normalizedPath =
                normalizeProblemPath(
                        problemPath
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

        if (
                problemJson == null ||
                problemJson.isBlank()
        ) {

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

            if (
                    problemNode == null ||
                    !problemNode.isObject()
            ) {

                throw new IllegalStateException(
                        "Invalid problem.json format."
                );
            }

            String title =
                    text(
                            problemNode,
                            "title"
                    ).trim();

            if (title.isBlank()) {

                throw new IllegalStateException(
                        "Problem title is required."
                );
            }

            CodingProblem problem =
                    codingProblemRepository
                            .findByTitleIgnoreCase(
                                    title
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

        } catch (IllegalStateException exception) {

            throw exception;

        } catch (Exception exception) {

            throw new IllegalStateException(
                    "Unable to import GitHub coding problem: "
                            + normalizedPath,
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
                ).trim();

        String difficulty =
                text(
                        node,
                        "difficulty"
                )
                .trim()
                .toUpperCase();

        if (title.isBlank()) {

            throw new IllegalStateException(
                    "Problem title is required."
            );
        }

        if (difficulty.isBlank()) {
            difficulty = "MEDIUM";
        }

        problem.setTitle(title);

        problem.setDescription(
                text(
                        node,
                        "description"
                )
        );

        problem.setDifficulty(
                normalizeDifficulty(
                        difficulty
                )
        );

        problem.setTags(
                stringList(
                        firstExistingNode(
                                node,
                                "tags",
                                "topics"
                        )
                )
        );

        problem.setInputExample(
                firstText(
                        node,
                        "inputExample",
                        "input",
                        "exampleInput"
                )
        );

        problem.setOutputExample(
                firstText(
                        node,
                        "outputExample",
                        "output",
                        "exampleOutput"
                )
        );

        problem.setConstraints(
                stringList(
                        firstExistingNode(
                                node,
                                "constraints",
                                "constraint"
                        )
                )
        );

        problem.setStarterCode(
                extractStarterCode(
                        node
                )
        );

        problem.setLanguageConfigurations(
                extractLanguageConfigurations(
                        node
                )
        );

        problem.setFunctionName(
                firstText(
                        node,
                        "functionName",
                        "methodName"
                )
        );

        problem.setFunctionSignature(
                firstText(
                        node,
                        "functionSignature",
                        "signature"
                )
        );

        problem.setReturnType(
                firstText(
                        node,
                        "returnType"
                )
        );

        problem.setParameterTypes(
                extractParameterTypes(
                        node
                )
        );

        problem.setMinimumExperienceLevel(
                extractMinimumExperienceLevel(
                        node
                )
        );

        problem.setActive(
                node.path("active")
                        .isMissingNode()
                        ||
                        node.path("active")
                                .asBoolean(true)
        );
    }

    private String extractStarterCode(
            JsonNode node
    ) {

        JsonNode starterCode =
                firstExistingNode(
                        node,
                        "starterCodes",
                        "starterCode"
                );

        if (
                starterCode == null ||
                starterCode.isMissingNode() ||
                starterCode.isNull()
        ) {

            return "";
        }

        if (starterCode.isTextual()) {
            return starterCode.asText();
        }

        try {

            return objectMapper.writeValueAsString(
                    starterCode
            );

        } catch (Exception exception) {

            throw new IllegalStateException(
                    "Invalid starter code configuration.",
                    exception
            );
        }
    }

    private String extractLanguageConfigurations(
            JsonNode node
    ) {

        JsonNode configurations =
                firstExistingNode(
                        node,
                        "languageConfigurations",
                        "languages",
                        "languageConfig"
                );

        if (
                configurations == null ||
                configurations.isMissingNode() ||
                configurations.isNull()
        ) {

            return "";
        }

        if (configurations.isTextual()) {
            return configurations.asText();
        }

        try {

            return objectMapper.writeValueAsString(
                    configurations
            );

        } catch (Exception exception) {

            throw new IllegalStateException(
                    "Invalid language configuration.",
                    exception
            );
        }
    }

    private String extractParameterTypes(
            JsonNode node
    ) {

        JsonNode parameterTypes =
                firstExistingNode(
                        node,
                        "parameterTypes",
                        "parameters"
                );

        if (
                parameterTypes == null ||
                parameterTypes.isMissingNode() ||
                parameterTypes.isNull()
        ) {

            return "";
        }

        if (parameterTypes.isTextual()) {
            return parameterTypes.asText();
        }

        try {

            return objectMapper.writeValueAsString(
                    parameterTypes
            );

        } catch (Exception exception) {

            throw new IllegalStateException(
                    "Invalid parameter type configuration.",
                    exception
            );
        }
    }

    private Integer extractMinimumExperienceLevel(
            JsonNode node
    ) {

        JsonNode value =
                firstExistingNode(
                        node,
                        "minimumExperienceLevel",
                        "experienceLevel",
                        "minExperienceLevel"
                );

        if (
                value == null ||
                value.isMissingNode() ||
                value.isNull()
        ) {

            return 1;
        }

        if (
                value.isInt() ||
                value.isLong()
        ) {

            return Math.max(
                    1,
                    value.asInt(1)
            );
        }

        try {

            return Math.max(
                    1,
                    Integer.parseInt(
                            value.asText()
                                    .trim()
                    )
            );

        } catch (Exception exception) {

            return 1;
        }
    }

    private void deactivateExistingTestCases(
            CodingProblem problem
    ) {

        List<CodingTestCase> existingTestCases =
                codingTestCaseRepository
                        .findByProblemAndActiveTrueOrderByTestCaseNumberAsc(
                                problem
                        );

        if (
                existingTestCases == null ||
                existingTestCases.isEmpty()
        ) {

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

        if (
                testsJson == null ||
                testsJson.isBlank()
        ) {

            return;
        }

        try {

            JsonNode root =
                    objectMapper.readTree(
                            testsJson
                    );

            JsonNode testCasesNode;

            if (
                    root != null &&
                    root.isArray()
            ) {

                testCasesNode = root;

            } else {

                testCasesNode =
                        firstExistingNode(
                                root,
                                "testCases",
                                "tests",
                                "cases"
                        );
            }

            if (
                    testCasesNode == null ||
                    !testCasesNode.isArray()
            ) {

                throw new IllegalStateException(
                        "Invalid tests.json format: "
                                + testsJsonPath
                );
            }

            List<CodingTestCase> testCases =
                    new ArrayList<>();

            int testCaseNumber = 1;

            for (
                    JsonNode node :
                    testCasesNode
            ) {

                String input =
                        firstText(
                                node,
                                "input",
                                "stdin"
                        );

                String expectedOutput =
                        firstText(
                                node,
                                "expectedOutput",
                                "output",
                                "expected"
                        );

                if (
                        input.isBlank() &&
                        expectedOutput.isBlank()
                ) {

                    continue;
                }

                if (
                        expectedOutput.isBlank()
                ) {

                    throw new IllegalStateException(
                            "Expected output is required for test case "
                                    + testCaseNumber
                    );
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
                                        node.path("hidden")
                                                .asBoolean(false)
                                )
                                .active(true)
                                .build();

                testCases.add(
                        testCase
                );
            }

            if (
                    !testCases.isEmpty()
            ) {

                codingTestCaseRepository.saveAll(
                        testCases
                );
            }

        } catch (IllegalStateException exception) {

            throw exception;

        } catch (Exception exception) {

            throw new IllegalStateException(
                    "Unable to import GitHub test cases: "
                            + testsJsonPath,
                    exception
            );
        }
    }

    private List<String> extractProblemPaths(
            List<String> files
    ) {

        Set<String> uniquePaths =
                new LinkedHashSet<>();

        for (
                String file :
                files
        ) {

            if (
                    file == null ||
                    file.isBlank()
            ) {

                continue;
            }

            String normalized =
                    normalizePath(file);

            if (
                    !normalized
                            .toLowerCase()
                            .endsWith(
                                    "/problem.json"
                            )
            ) {

                continue;
            }

            String problemPath =
                    normalized.substring(
                            0,
                            normalized.length()
                                    - "/problem.json"
                                    .length()
                    );

            if (
                    !problemPath.isBlank()
            ) {

                uniquePaths.add(
                        problemPath
                );
            }
        }

        return new ArrayList<>(
                uniquePaths
        );
    }

    private JsonNode firstExistingNode(
            JsonNode node,
            String... fields
    ) {

        if (
                node == null ||
                fields == null
        ) {

            return null;
        }

        for (
                String field :
                fields
        ) {

            if (
                    field == null ||
                    field.isBlank()
            ) {

                continue;
            }

            JsonNode value =
                    node.path(field);

            if (
                    !value.isMissingNode() &&
                    !value.isNull()
            ) {

                return value;
            }
        }

        return null;
    }

    private String firstText(
            JsonNode node,
            String... fields
    ) {

        JsonNode value =
                firstExistingNode(
                        node,
                        fields
                );

        if (value == null) {
            return "";
        }

        if (
                value.isTextual() ||
                value.isNumber() ||
                value.isBoolean()
        ) {

            return value.asText();
        }

        return "";
    }

    private String text(
            JsonNode node,
            String field
    ) {

        if (
                node == null ||
                field == null ||
                field.isBlank()
        ) {

            return "";
        }

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
                node.isMissingNode() ||
                node.isNull()
        ) {

            return values;
        }

        if (node.isTextual()) {

            String value =
                    node.asText().trim();

            if (!value.isBlank()) {
                values.add(value);
            }

            return values;
        }

        if (!node.isArray()) {
            return values;
        }

        for (
                JsonNode item :
                node
        ) {

            if (
                    item == null ||
                    item.isNull()
            ) {

                continue;
            }

            String value =
                    item.asText().trim();

            if (!value.isBlank()) {
                values.add(value);
            }
        }

        return values;
    }

    private String normalizeDifficulty(
            String difficulty
    ) {

        String normalized =
                difficulty
                        .trim()
                        .toUpperCase();

        if (normalized.equals("EASY")) {
            return "EASY";
        }

        if (
                normalized.equals("MEDIUM") ||
                normalized.equals("INTERMEDIATE")
        ) {

            return "MEDIUM";
        }

        if (
                normalized.equals("HARD") ||
                normalized.equals("ADVANCED")
        ) {

            return "HARD";
        }

        return "MEDIUM";
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

        String normalized =
                normalizeRepository(
                        repository
                );

        if (
                normalized.isBlank() ||
                !normalized.contains("/")
        ) {

            throw new IllegalArgumentException(
                    "GitHub repository must use owner/repository format."
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

        String normalized =
                normalizeProblemPath(
                        problemPath
                );

        if (normalized.isBlank()) {

            throw new IllegalArgumentException(
                    "Problem path is invalid."
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
                        "\\.git$",
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
                .replace(
                        "\\",
                        "/"
                )
                .replaceAll(
                        "^/+",
                        ""
                )
                .replaceAll(
                        "/+$",
                        ""
                );
    }

    private String normalizeProblemPath(
            String path
    ) {

        String normalized =
                normalizePath(path);

        if (
                normalized
                        .toLowerCase()
                        .endsWith(
                                "/problem.json"
                        )
        ) {

            return normalized.substring(
                    0,
                    normalized.length()
                            - "/problem.json".length()
            );
        }

        return normalized;
    }

    public static class BulkImportResult {

        private final String repository;
        private final int discovered;
        private final int imported;
        private final int failed;
        private final List<String> failedPaths;
        private final List<String> importedPaths;

        public BulkImportResult(
                String repository,
                int discovered,
                int imported,
                int failed,
                List<String> failedPaths,
                List<String> importedPaths
        ) {

            this.repository =
                    repository;

            this.discovered =
                    discovered;

            this.imported =
                    imported;

            this.failed =
                    failed;

            this.failedPaths =
                    failedPaths == null
                            ? List.of()
                            : List.copyOf(
                                    failedPaths
                            );

            this.importedPaths =
                    importedPaths == null
                            ? List.of()
                            : List.copyOf(
                                    importedPaths
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

        public List<String> getImportedPaths() {
            return importedPaths;
        }
    }
}