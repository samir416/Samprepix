package com.aiinterview.backend.service.coding;

import com.aiinterview.backend.entity.CodingProblem;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

@Service
public class FunctionExecutionWrapperService {

    private final ObjectMapper objectMapper;

    public FunctionExecutionWrapperService() {
        this.objectMapper = new ObjectMapper();
    }

    public String buildExecutableCode(
            CodingProblem problem,
            String language,
            String userCode,
            String input
    ) {

        validateProblem(problem);
        validateLanguage(language);
        validateCode(userCode);

        LanguageConfiguration configuration =
                getConfiguration(
                        problem,
                        language
                );

        if (configuration == null) {

            throw new IllegalArgumentException(
                    "Selected language is not configured for this problem."
            );
        }

        String template =
                configuration.getExecutionTemplate();

        if (template == null ||
                template.isBlank()) {

            return userCode;
        }

        return applyTemplate(
                template,
                userCode,
                input,
                problem
        );
    }

    public String getStarterCode(
            CodingProblem problem,
            String language
    ) {

        if (problem == null ||
                language == null ||
                language.isBlank()) {

            return "";
        }

        LanguageConfiguration configuration =
                getConfiguration(
                        problem,
                        language
                );

        if (configuration == null ||
                configuration.getStarterCode() == null) {

            return "";
        }

        return configuration
                .getStarterCode();
    }

    public String getRuntimeLanguage(
            CodingProblem problem,
            String language
    ) {

        LanguageConfiguration configuration =
                requireConfiguration(
                        problem,
                        language
                );

        String runtimeLanguage =
                configuration
                        .getRuntimeLanguage();

        if (runtimeLanguage == null ||
                runtimeLanguage.isBlank()) {

            throw new IllegalArgumentException(
                    "Runtime language is not configured for the selected language."
            );
        }

        return runtimeLanguage.trim();
    }

    public String getRuntimeVersion(
            CodingProblem problem,
            String language
    ) {

        LanguageConfiguration configuration =
                requireConfiguration(
                        problem,
                        language
                );

        String runtimeVersion =
                configuration
                        .getRuntimeVersion();

        if (runtimeVersion == null ||
                runtimeVersion.isBlank()) {

            throw new IllegalArgumentException(
                    "Runtime version is not configured for the selected language."
            );
        }

        return runtimeVersion.trim();
    }

    public String getFileName(
            CodingProblem problem,
            String language
    ) {

        LanguageConfiguration configuration =
                requireConfiguration(
                        problem,
                        language
                );

        String fileName =
                configuration
                        .getFileName();

        if (fileName == null ||
                fileName.isBlank()) {

            throw new IllegalArgumentException(
                    "Source filename is not configured for the selected language."
            );
        }

        return fileName.trim();
    }

    public Map<String, LanguageConfiguration>
    getLanguageConfigurations(
            CodingProblem problem
    ) {

        if (problem == null) {

            return Collections.emptyMap();
        }

        String configurationJson =
                problem.getLanguageConfigurations();

        if (configurationJson == null ||
                configurationJson.isBlank()) {

            return Collections.emptyMap();
        }

        try {

            JsonNode root =
                    objectMapper.readTree(
                            configurationJson
                    );

            if (root == null ||
                    !root.isObject()) {

                throw new IllegalStateException(
                        "Language configuration must be a JSON object."
                );
            }

            Map<String, LanguageConfiguration>
                    configurations =
                    new LinkedHashMap<>();

            Iterator<Map.Entry<String, JsonNode>>
                    fields =
                    root.fields();

            while (fields.hasNext()) {

                Map.Entry<String, JsonNode> entry =
                        fields.next();

                String key =
                        normalizeLanguage(
                                entry.getKey()
                        );

                if (key.isBlank()) {
                    continue;
                }

                LanguageConfiguration configuration =
                        objectMapper.treeToValue(
                                entry.getValue(),
                                LanguageConfiguration.class
                        );

                if (configuration == null) {
                    continue;
                }

                if (
                        configuration.getDisplayName() == null ||
                        configuration.getDisplayName().isBlank()
                ) {

                    configuration.setDisplayName(
                            entry.getKey()
                    );
                }

                configurations.put(
                        key,
                        configuration
                );
            }

            return configurations;

        } catch (Exception exception) {

            throw new IllegalStateException(
                    "Invalid language configuration for coding problem.",
                    exception
            );
        }
    }

    public boolean supportsLanguage(
            CodingProblem problem,
            String language
    ) {

        return getConfiguration(
                problem,
                language
        ) != null;
    }

    private LanguageConfiguration requireConfiguration(
            CodingProblem problem,
            String language
    ) {

        validateProblem(problem);
        validateLanguage(language);

        LanguageConfiguration configuration =
                getConfiguration(
                        problem,
                        language
                );

        if (configuration == null) {

            throw new IllegalArgumentException(
                    "Selected language is not configured for this problem."
            );
        }

        return configuration;
    }

    private LanguageConfiguration getConfiguration(
            CodingProblem problem,
            String language
    ) {

        if (problem == null ||
                language == null ||
                language.isBlank()) {

            return null;
        }

        Map<String, LanguageConfiguration>
                configurations =
                getLanguageConfigurations(
                        problem
                );

        if (configurations.isEmpty()) {

            return null;
        }

        String normalizedLanguage =
                normalizeLanguage(
                        language
                );

        LanguageConfiguration direct =
                configurations.get(
                        normalizedLanguage
                );

        if (direct != null) {

            return direct;
        }

        for (
                Map.Entry<String, LanguageConfiguration>
                        entry :
                        configurations.entrySet()
        ) {

            LanguageConfiguration configuration =
                    entry.getValue();

            if (configuration == null) {
                continue;
            }

            if (
                    configuration.getDisplayName() != null &&
                    normalizeLanguage(
                            configuration.getDisplayName()
                    ).equals(
                            normalizedLanguage
                    )
            ) {

                return configuration;
            }

            if (
                    configuration.getRuntimeLanguage() != null &&
                    normalizeLanguage(
                            configuration.getRuntimeLanguage()
                    ).equals(
                            normalizedLanguage
                    )
            ) {

                return configuration;
            }
        }

        return null;
    }

    private String applyTemplate(
            String template,
            String userCode,
            String input,
            CodingProblem problem
    ) {

        String result =
                template.replace(
                        "{{USER_CODE}}",
                        userCode
                );

        result =
                result.replace(
                        "{{INPUT}}",
                        input == null
                                ? ""
                                : input
                );

        result =
                result.replace(
                        "{{FUNCTION_NAME}}",
                        safe(
                                problem.getFunctionName()
                        )
                );

        result =
                result.replace(
                        "{{FUNCTION_SIGNATURE}}",
                        safe(
                                problem.getFunctionSignature()
                        )
                );

        result =
                result.replace(
                        "{{RETURN_TYPE}}",
                        safe(
                                problem.getReturnType()
                        )
                );

        result =
                result.replace(
                        "{{PARAMETER_TYPES}}",
                        safe(
                                problem.getParameterTypes()
                        )
                );

        return result;
    }

    private String normalizeLanguage(
            String language
    ) {

        if (language == null) {
            return "";
        }

        return language
                .trim()
                .toLowerCase(
                        Locale.ROOT
                )
                .replace(
                        "_",
                        "-"
                );
    }

    private String safe(
            String value
    ) {

        return value == null
                ? ""
                : value;
    }

    private void validateProblem(
            CodingProblem problem
    ) {

        if (problem == null) {

            throw new IllegalArgumentException(
                    "Coding problem is required."
            );
        }
    }

    private void validateLanguage(
            String language
    ) {

        if (language == null ||
                language.isBlank()) {

            throw new IllegalArgumentException(
                    "Programming language is required."
            );
        }
    }

    private void validateCode(
            String userCode
    ) {

        if (userCode == null ||
                userCode.isBlank()) {

            throw new IllegalArgumentException(
                    "Code cannot be empty."
            );
        }
    }

    public static class LanguageConfiguration {

        private String displayName;

        private String runtimeLanguage;

        private String runtimeVersion;

        private String fileName;

        private String starterCode;

        private String executionTemplate;

        public String getDisplayName() {
            return displayName;
        }

        public void setDisplayName(
                String displayName
        ) {
            this.displayName =
                    displayName;
        }

        public String getRuntimeLanguage() {
            return runtimeLanguage;
        }

        public void setRuntimeLanguage(
                String runtimeLanguage
        ) {
            this.runtimeLanguage =
                    runtimeLanguage;
        }

        public String getRuntimeVersion() {
            return runtimeVersion;
        }

        public void setRuntimeVersion(
                String runtimeVersion
        ) {
            this.runtimeVersion =
                    runtimeVersion;
        }

        public String getFileName() {
            return fileName;
        }

        public void setFileName(
                String fileName
        ) {
            this.fileName =
                    fileName;
        }

        public String getStarterCode() {
            return starterCode;
        }

        public void setStarterCode(
                String starterCode
        ) {
            this.starterCode =
                    starterCode;
        }

        public String getExecutionTemplate() {
            return executionTemplate;
        }

        public void setExecutionTemplate(
                String executionTemplate
        ) {
            this.executionTemplate =
                    executionTemplate;
        }
    }
}