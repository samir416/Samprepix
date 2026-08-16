package com.aiinterview.backend.service.coding;

import com.aiinterview.backend.entity.CodingProblem;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;

import java.util.Collections;
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

        if (problem == null) {

            throw new IllegalArgumentException(
                    "Coding problem is required."
            );
        }

        if (language == null ||
                language.isBlank()) {

            throw new IllegalArgumentException(
                    "Programming language is required."
            );
        }

        if (userCode == null ||
                userCode.isBlank()) {

            throw new IllegalArgumentException(
                    "Code cannot be empty."
            );
        }

        Map<String, LanguageConfiguration> configurations =
                getLanguageConfigurations(
                        problem
                );

        LanguageConfiguration configuration =
                findConfiguration(
                        configurations,
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

        Map<String, LanguageConfiguration> configurations =
                getLanguageConfigurations(
                        problem
                );

        LanguageConfiguration configuration =
                findConfiguration(
                        configurations,
                        language
                );

        if (configuration == null) {

            return "";
        }

        return configuration.getStarterCode() == null
                ? ""
                : configuration.getStarterCode();
    }

    public String getRuntimeLanguage(
            CodingProblem problem,
            String language
    ) {

        LanguageConfiguration configuration =
                getConfiguration(
                        problem,
                        language
                );

        if (configuration == null ||
                configuration.getRuntimeLanguage() == null ||
                configuration.getRuntimeLanguage().isBlank()) {

            return language;
        }

        return configuration
                .getRuntimeLanguage()
                .trim();
    }

    public String getRuntimeVersion(
            CodingProblem problem,
            String language
    ) {

        LanguageConfiguration configuration =
                getConfiguration(
                        problem,
                        language
                );

        if (configuration == null) {

            return null;
        }

        return configuration.getRuntimeVersion();
    }

    public String getFileName(
            CodingProblem problem,
            String language
    ) {

        LanguageConfiguration configuration =
                getConfiguration(
                        problem,
                        language
                );

        if (configuration == null ||
                configuration.getFileName() == null ||
                configuration.getFileName().isBlank()) {

            return "Main.txt";
        }

        return configuration
                .getFileName()
                .trim();
    }

    private LanguageConfiguration getConfiguration(
            CodingProblem problem,
            String language
    ) {

        Map<String, LanguageConfiguration> configurations =
                getLanguageConfigurations(
                        problem
                );

        return findConfiguration(
                configurations,
                language
        );
    }

    private Map<String, LanguageConfiguration>
    getLanguageConfigurations(
            CodingProblem problem
    ) {

        String configurationJson =
                problem.getLanguageConfigurations();

        if (configurationJson == null ||
                configurationJson.isBlank()) {

            return Collections.emptyMap();
        }

        try {

            return objectMapper.readValue(
                    configurationJson,
                    new TypeReference<
                            Map<String, LanguageConfiguration>
                            >() {}
            );

        } catch (Exception exception) {

            throw new IllegalStateException(
                    "Invalid language configuration for coding problem.",
                    exception
            );
        }
    }

    private LanguageConfiguration findConfiguration(
            Map<String, LanguageConfiguration> configurations,
            String language
    ) {

        if (configurations.isEmpty()) {

            return null;
        }

        LanguageConfiguration direct =
                configurations.get(
                        language
                                .trim()
                                .toLowerCase()
                );

        if (direct != null) {

            return direct;
        }

        for (
                Map.Entry<String, LanguageConfiguration> entry :
                configurations.entrySet()
        ) {

            if (
                    entry.getKey()
                            .equalsIgnoreCase(
                                    language.trim()
                            )
            ) {

                return entry.getValue();
            }

            LanguageConfiguration value =
                    entry.getValue();

            if (
                    value.getDisplayName() != null &&
                    value.getDisplayName()
                            .equalsIgnoreCase(
                                    language.trim()
                            )
            ) {

                return value;
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

    private String safe(
            String value
    ) {

        return value == null
                ? ""
                : value;
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
            this.displayName = displayName;
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