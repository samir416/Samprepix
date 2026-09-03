package com.aiinterview.backend.config;

import com.aiinterview.backend.entity.CodingProblem;
import com.aiinterview.backend.entity.CodingTestCase;
import com.aiinterview.backend.repository.CodingProblemRepository;
import com.aiinterview.backend.repository.CodingTestCaseRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityManager;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

@Configuration
public class CodingProblemDatasetImporter {

    private static final int BATCH_SIZE = 100;

    @Value("${coding.problems.import-resource:}")
    private String importResource;

    @Bean
    @Order(1)
    CommandLineRunner importCodingProblemDataset(
            CodingProblemRepository problemRepository,
            CodingTestCaseRepository testCaseRepository,
            ObjectMapper objectMapper,
            ResourceLoader resourceLoader,
            PlatformTransactionManager transactionManager,
            EntityManager entityManager
    ) {
        return args -> {
            if (importResource == null || importResource.isBlank()) {
                return;
            }

            Resource resource = resourceLoader.getResource(importResource.trim());
            if (!resource.exists()) {
                throw new IllegalStateException(
                        "Configured coding problem dataset was not found: " + importResource
                );
            }

            DatasetReport report = new DatasetReport();
            TransactionTemplate transactionTemplate = new TransactionTemplate(transactionManager);

            // Pre-populate duplicate detection sets in O(1) memory
            Set<String> existingSourceIds = new HashSet<>(problemRepository.findAllSourceIds());
            Set<String> existingSlugs = new HashSet<>(problemRepository.findAllSlugs());
            Set<String> existingTitles = new HashSet<>(problemRepository.findAllTitlesLower());

            List<ImportedProblem> batch = new ArrayList<>(BATCH_SIZE);

            long startTime = System.currentTimeMillis();
            System.out.printf("Starting coding problem dataset import from: %s%n", importResource);

            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(resource.getInputStream(), StandardCharsets.UTF_8), 65536)) {
                String line;
                int lineNumber = 0;

                while ((line = reader.readLine()) != null) {
                    lineNumber++;
                    if (line.isBlank()) {
                        continue;
                    }

                    report.records++;
                    try {
                        JsonNode node = objectMapper.readTree(line);
                        ImportedProblem imported = parseAndValidate(
                                node,
                                objectMapper,
                                lineNumber,
                                existingSourceIds,
                                existingSlugs
                        );

                        String titleLower = imported.problem().getTitle().trim().toLowerCase(Locale.ROOT);
                        if (existingTitles.contains(titleLower)) {
                            report.skippedDuplicates++;
                            continue;
                        }

                        existingTitles.add(titleLower);
                        batch.add(imported);

                        if (batch.size() >= BATCH_SIZE) {
                            saveBatchInTransaction(transactionTemplate, problemRepository, testCaseRepository, entityManager, batch);
                            report.imported += batch.size();
                            batch.clear();
                        }

                        if (report.records % 1000 == 0) {
                            System.out.printf(
                                    "Coding dataset import progress: processed=%d, imported=%d, skippedDuplicates=%d, invalid=%d%n",
                                    report.records,
                                    report.imported,
                                    report.skippedDuplicates,
                                    report.invalid
                            );
                        }
                    } catch (Exception exception) {
                        report.invalid++;
                        report.invalidLines.add(lineNumber + ": " + exception.getMessage());
                    }
                }

                if (!batch.isEmpty()) {
                    saveBatchInTransaction(transactionTemplate, problemRepository, testCaseRepository, entityManager, batch);
                    report.imported += batch.size();
                    batch.clear();
                }
            }

            long elapsed = System.currentTimeMillis() - startTime;
            System.out.printf(
                    "Coding dataset import completed in %d ms: records=%d imported=%d skippedDuplicates=%d invalid=%d%n",
                    elapsed,
                    report.records,
                    report.imported,
                    report.skippedDuplicates,
                    report.invalid
            );

            if (!report.invalidLines.isEmpty()) {
                report.invalidLines.stream().limit(20).forEach(
                        message -> System.err.println("Coding dataset invalid: " + message)
                );
            }
        };
    }

    private void saveBatchInTransaction(
            TransactionTemplate transactionTemplate,
            CodingProblemRepository problemRepository,
            CodingTestCaseRepository testCaseRepository,
            EntityManager entityManager,
            List<ImportedProblem> batch
    ) {
        transactionTemplate.executeWithoutResult(status -> {
            List<CodingProblem> problemsToSave = new ArrayList<>(batch.size());
            for (ImportedProblem ip : batch) {
                problemsToSave.add(ip.problem());
            }

            List<CodingProblem> savedProblems = problemRepository.saveAll(problemsToSave);
            List<CodingTestCase> testCasesToSave = new ArrayList<>(batch.size() * 4);

            for (int i = 0; i < batch.size(); i++) {
                CodingProblem savedProblem = savedProblems.get(i);
                for (CodingTestCase testCase : batch.get(i).testCases()) {
                    testCase.setProblem(savedProblem);
                    testCasesToSave.add(testCase);
                }
            }

            testCaseRepository.saveAll(testCasesToSave);
            entityManager.flush();
            entityManager.clear();
        });
    }

    private ImportedProblem parseAndValidate(
            JsonNode node,
            ObjectMapper objectMapper,
            int lineNumber,
            Set<String> sourceIds,
            Set<String> slugs
    ) throws Exception {
        if (node == null || !node.isObject()) {
            throw new IllegalArgumentException("line " + lineNumber + " must be a JSON object");
        }

        String sourceId = requiredText(node, "sourceId");
        String slug = requiredText(node, "slug").toLowerCase(Locale.ROOT);
        String title = requiredText(node, "title");
        String description = requiredText(node, "description");
        String difficulty = requiredText(node, "difficulty").toUpperCase(Locale.ROOT);

        if (!Set.of("EASY", "MEDIUM", "HARD").contains(difficulty)) {
            throw new IllegalArgumentException("difficulty must be EASY, MEDIUM, or HARD");
        }
        if (!slug.matches("[a-z0-9]+(?:-[a-z0-9]+)*")) {
            throw new IllegalArgumentException("slug must be lowercase kebab-case");
        }
        if (!sourceIds.add(sourceId) || !slugs.add(slug)) {
            throw new IllegalArgumentException("duplicate sourceId or slug in dataset: " + sourceId + " / " + slug);
        }

        List<String> tags = readTextList(node.path("tags"), "tags");
        List<String> constraints = readTextList(node.path("constraints"), "constraints");
        if (tags.isEmpty()) {
            throw new IllegalArgumentException("tags must contain at least one value");
        }

        JsonNode configurations = node.path("languageConfigurations");
        if (!configurations.isObject() || configurations.isEmpty()) {
            throw new IllegalArgumentException("languageConfigurations must be a non-empty object");
        }

        JsonNode testCasesNode = node.path("testCases");
        if (!testCasesNode.isArray() || testCasesNode.isEmpty()) {
            throw new IllegalArgumentException("testCases must be a non-empty array");
        }

        List<CodingTestCase> testCases = new ArrayList<>();
        Set<Integer> numbers = new HashSet<>();
        boolean hasPublic = false;
        boolean hasHidden = false;

        for (JsonNode testCaseNode : testCasesNode) {
            int number = testCaseNode.path("testCaseNumber").asInt(0);
            String input = requiredText(testCaseNode, "input");
            String expectedOutput = requiredText(testCaseNode, "expectedOutput");
            boolean hidden = testCaseNode.path("hidden").asBoolean(false);

            if (number < 1 || !numbers.add(number)) {
                throw new IllegalArgumentException("testCaseNumber must be unique and positive");
            }

            hasPublic |= !hidden;
            hasHidden |= hidden;
            testCases.add(CodingTestCase.builder()
                    .testCaseNumber(number)
                    .input(input)
                    .expectedOutput(expectedOutput)
                    .hidden(hidden)
                    .active(true)
                    .build());
        }

        if (!hasPublic || !hasHidden) {
            throw new IllegalArgumentException("each problem requires public and hidden test cases");
        }

        CodingProblem problem = CodingProblem.builder()
                .sourceId(sourceId)
                .slug(slug)
                .title(title)
                .description(description)
                .difficulty(difficulty)
                .tags(tags)
                .constraints(constraints)
                .inputExample(optionalText(node, "inputExample"))
                .outputExample(optionalText(node, "outputExample"))
                .starterCode(node.path("starterCode").isMissingNode()
                        ? ""
                        : objectMapper.writeValueAsString(node.get("starterCode")))
                .languageConfigurations(objectMapper.writeValueAsString(configurations))
                .functionName(optionalText(node, "functionName"))
                .functionSignature(optionalText(node, "functionSignature"))
                .returnType(optionalText(node, "returnType"))
                .parameterTypes(optionalText(node, "parameterTypes"))
                .minimumExperienceLevel(Math.max(1, node.path("minimumExperienceLevel").asInt(1)))
                .active(!node.has("active") || node.path("active").asBoolean(true))
                .build();

        return new ImportedProblem(sourceId, slug, problem, testCases);
    }

    private String requiredText(JsonNode node, String field) {
        String value = optionalText(node, field);
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(field + " is required");
        }
        return value.trim();
    }

    private String optionalText(JsonNode node, String field) {
        JsonNode value = node == null ? null : node.get(field);
        return value == null || !value.isTextual() ? null : value.asText();
    }

    private List<String> readTextList(JsonNode node, String field) {
        if (!node.isArray()) {
            throw new IllegalArgumentException(field + " must be an array");
        }
        List<String> values = new ArrayList<>();
        for (JsonNode value : node) {
            if (!value.isTextual() || value.asText().isBlank()) {
                throw new IllegalArgumentException(field + " contains an invalid value");
            }
            values.add(value.asText().trim());
        }
        return values;
    }

    private record ImportedProblem(
            String sourceId,
            String slug,
            CodingProblem problem,
            List<CodingTestCase> testCases
    ) {
    }

    private static final class DatasetReport {
        private int records;
        private int imported;
        private int skippedDuplicates;
        private int invalid;
        private final List<String> invalidLines = new ArrayList<>();
    }
}
