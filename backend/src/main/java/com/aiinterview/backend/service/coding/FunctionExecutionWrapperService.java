package com.aiinterview.backend.service.coding;

import com.aiinterview.backend.config.CentralLanguageRegistry;
import com.aiinterview.backend.entity.CodingProblem;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class FunctionExecutionWrapperService {

    private final ObjectMapper objectMapper;

    public FunctionExecutionWrapperService() {
        this.objectMapper = new ObjectMapper();
    }

    public String getRuntimeLanguage(
            CodingProblem problem,
            String language
    ) {
        Map<String, Object> config = getLanguageConfiguration(problem, language);
        String runtimeLanguage = stringValue(config.get("runtimeLanguage"));
        if (runtimeLanguage != null && !runtimeLanguage.isBlank()) {
            return runtimeLanguage.trim();
        }
        CentralLanguageRegistry.LanguageSpec spec = CentralLanguageRegistry.get(language);
        return spec != null ? spec.runtimeLanguage() : language;
    }

    public String getRuntimeVersion(
            CodingProblem problem,
            String language
    ) {
        Map<String, Object> config = getLanguageConfiguration(problem, language);
        String version = stringValue(config.get("runtimeVersion"));
        if (version != null && !version.isBlank()) {
            return version.trim();
        }
        CentralLanguageRegistry.LanguageSpec spec = CentralLanguageRegistry.get(language);
        return spec != null ? spec.runtimeVersion() : "*";
    }

    public String getFileName(
            CodingProblem problem,
            String language
    ) {
        Map<String, Object> config = getLanguageConfiguration(problem, language);
        String fileName = stringValue(config.get("fileName"));
        if (fileName != null && !fileName.isBlank()) {
            return fileName.trim();
        }
        CentralLanguageRegistry.LanguageSpec spec = CentralLanguageRegistry.get(language);
        return spec != null ? spec.fileName() : "main";
    }

    public String getStarterCode(
            CodingProblem problem,
            String language
    ) {

        Map<String, Object> configuration =
                getLanguageConfiguration(
                        problem,
                        language
                );

        String starterCode =
                stringValue(
                        configuration.get("starterCode")
                );

        return starterCode == null
                ? ""
                : starterCode;
    }

    public String buildExecutableCode(
            CodingProblem problem,
            String language,
            String userCode,
            String input
    ) {

        if (problem == null) {
            throw new IllegalArgumentException(
                    "Coding problem cannot be null."
            );
        }

        if (language == null || language.isBlank()) {
            throw new IllegalArgumentException(
                    "Programming language cannot be empty."
            );
        }

        if (userCode == null || userCode.isBlank()) {
            throw new IllegalArgumentException(
                    "Code cannot be empty."
            );
        }

        Map<String, Object> configuration =
                getLanguageConfiguration(
                        problem,
                        language
                );

        String runtimeLanguage =
                stringValue(
                        configuration.get("runtimeLanguage")
                );

        if (runtimeLanguage == null) {
            throw new IllegalArgumentException(
                    "Runtime language is not configured for: "
                            + language
            );
        }

        String testInput =
                input == null
                        ? ""
                        : input.trim();

        if (!isLegacyFunction(problem.getFunctionName())) {
            String template = stringValue(configuration.get("executionTemplate"));
            if (template != null && !template.isBlank() && !template.trim().equals("{{USER_CODE}}")) {
                String code = buildConfiguredCode(configuration, userCode, testInput);
                if ("java".equalsIgnoreCase(runtimeLanguage.trim()) && !code.contains("class ")) {
                    return buildGenericJavaCode(code, testInput);
                }
                return code;
            }
            if ("java".equalsIgnoreCase(runtimeLanguage.trim())) {
                return buildGenericJavaCode(userCode, testInput);
            }
            return userCode;
        }

        return switch (
                runtimeLanguage
                        .trim()
                        .toLowerCase()
        ) {

            case "java" ->
                    buildJavaCode(
                            problem,
                            userCode,
                            testInput
                    );

            case "python", "python3" ->
                    buildPythonCode(
                            problem,
                            userCode,
                            testInput
                    );

            case "kotlin" ->
                    buildKotlinCode(
                            problem,
                            userCode,
                            testInput
                    );

            case "go" ->
                    buildGoCode(
                            problem,
                            userCode,
                            testInput
                    );

            case "rust" ->
                    buildRustCode(
                            problem,
                            userCode,
                            testInput
                    );

            default ->
                    buildConfiguredCode(
                            configuration,
                            userCode,
                            testInput
                    );
        };
    }

    private boolean isLegacyFunction(String functionName) {
        if (functionName == null || functionName.isBlank()) {
            return false;
        }
        return switch (functionName) {
            case "twoSum", "isValid", "maxProfit", "search",
                 "lengthOfLongestSubstring", "productExceptSelf", "merge",
                 "numIslands", "canFinish", "trap" -> true;
            default -> false;
        };
    }

    private String buildGenericJavaCode(String userCode, String input) {
        if (userCode.contains("class Main") || (userCode.contains("class ") && userCode.contains("static void main"))) {
            return userCode;
        }
        return """
import java.util.*;
import java.io.*;

public class Main {
%s
}
""".formatted(indent(userCode, 4));
    }

    public Map<String, Object> getLanguageConfiguration(
            CodingProblem problem,
            String language
    ) {

        if (problem == null) {
            throw new IllegalArgumentException(
                    "Coding problem cannot be null."
            );
        }

        if (language == null || language.isBlank()) {
            throw new IllegalArgumentException(
                    "Programming language cannot be empty."
            );
        }

        String configurations =
                problem.getLanguageConfigurations();

        if (
                configurations == null ||
                configurations.isBlank()
        ) {
            throw new IllegalArgumentException(
                    "Language configurations are not available for this problem."
            );
        }

        try {

            Map<String, Object> languageConfigurations =
                    objectMapper.readValue(
                            configurations,
                            new TypeReference<
                                    LinkedHashMap<String, Object>
                            >() {
                            }
                    );

            Map<String, Object> configuration =
                    findConfiguration(
                            languageConfigurations,
                            language
                    );

            if (configuration == null && !isLegacyFunction(problem.getFunctionName())) {
                CentralLanguageRegistry.LanguageSpec spec = CentralLanguageRegistry.get(language);
                if (spec != null) {
                    configuration = CentralLanguageRegistry.toConfigurationMap(spec);
                }
            }

            if (configuration == null) {
                throw new IllegalArgumentException(
                        "Language is not supported for this problem: "
                                + language
                );
            }

            return configuration;

        } catch (IllegalArgumentException exception) {

            throw exception;

        } catch (Exception exception) {

            throw new IllegalArgumentException(
                    "Invalid language configuration.",
                    exception
            );
        }
    }

    private Map<String, Object> findConfiguration(
            Map<String, Object> configurations,
            String requestedLanguage
    ) {

        if (
                configurations == null ||
                configurations.isEmpty()
        ) {
            return null;
        }

        String normalized =
                requestedLanguage
                        .trim()
                        .toLowerCase();

        for (
                Map.Entry<String, Object> entry :
                configurations.entrySet()
        ) {

            if (
                    !(entry.getValue()
                            instanceof Map<?, ?> rawMap)
            ) {
                continue;
            }

            Map<String, Object> configuration =
                    convertMap(rawMap);

            if (
                    matches(
                            normalized,
                            entry.getKey()
                    ) ||
                    matches(
                            normalized,
                            stringValue(
                                    configuration.get(
                                            "displayName"
                                    )
                            )
                    ) ||
                    matches(
                            normalized,
                            stringValue(
                                    configuration.get(
                                            "runtimeLanguage"
                                    )
                            )
                    )
            ) {

                return configuration;
            }

            Object aliases =
                    configuration.get("aliases");

            if (aliases instanceof Iterable<?> values) {

                for (Object alias : values) {

                    if (
                            matches(
                                    normalized,
                                    stringValue(alias)
                            )
                    ) {
                        return configuration;
                    }
                }
            }
        }

        return null;
    }

    private String buildConfiguredCode(
            Map<String, Object> configuration,
            String userCode,
            String input
    ) {

        String template =
                stringValue(
                        configuration.get(
                                "executionTemplate"
                        )
                );

        if (
                template != null &&
                !template.isBlank()
        ) {

            return template
                    .replace(
                            "{{USER_CODE}}",
                            userCode
                    )
                    .replace(
                            "{{TEST_INPUT}}",
                            input
                    );
        }

        return userCode;
    }

    private String buildJavaCode(
            CodingProblem problem,
            String userCode,
            String input
    ) {

        return """
import java.util.*;
import java.io.*;

public class Main {

%s

    public static void main(String[] args) throws Exception {
%s
    }
}
""".formatted(
                indent(userCode, 4),
                indent(
                        buildJavaInvocation(
                                problem,
                                input
                        ),
                        8
                )
        );
    }

    private String buildJavaInvocation(
            CodingProblem problem,
            String input
    ) {

        String functionName =
                problem.getFunctionName();

        if ("twoSum".equals(functionName)) {

            String[] parts =
                    splitTwoSumInput(input);

            return """
int[] nums = %s;
int target = %s;
System.out.println(
        Arrays.toString(
                twoSum(nums, target)
        )
);
""".formatted(
                    javaIntArray(parts[0]),
                    parseInteger(parts[1])
            );
        }

        if ("isValid".equals(functionName)) {

            return """
String s = %s;
System.out.println(isValid(s));
""".formatted(
                    javaString(
                            extractStringValue(input)
                    )
            );
        }

        if ("maxProfit".equals(functionName)) {

            return """
int[] prices = %s;
System.out.println(maxProfit(prices));
""".formatted(
                    javaIntArray(input)
            );
        }

        if ("search".equals(functionName)) {

            String[] parts =
                    splitTwoSumInput(input);

            return """
int[] nums = %s;
int target = %s;
System.out.println(search(nums, target));
""".formatted(
                    javaIntArray(parts[0]),
                    parseInteger(parts[1])
            );
        }

        if ("lengthOfLongestSubstring".equals(functionName)) {

            return """
String s = %s;
System.out.println(
        lengthOfLongestSubstring(s)
);
""".formatted(
                    javaString(
                            extractStringValue(input)
                    )
            );
        }

        if ("productExceptSelf".equals(functionName)) {

            return """
int[] nums = %s;
System.out.println(
        Arrays.toString(
                productExceptSelf(nums)
        )
);
""".formatted(
                    javaIntArray(input)
            );
        }

        if ("merge".equals(functionName)) {

            return """
int[][] intervals = %s;
System.out.println(
        Arrays.deepToString(
                merge(intervals)
        )
);
""".formatted(
                    javaIntMatrix(input)
            );
        }

        if ("numIslands".equals(functionName)) {

            return """
char[][] grid = %s;
System.out.println(
        numIslands(grid)
);
""".formatted(
                    javaCharMatrix(input)
            );
        }

        if ("canFinish".equals(functionName)) {

            String[] parts =
                    splitCourseScheduleInput(input);

            return """
int numCourses = %s;
int[][] prerequisites = %s;
System.out.println(
        canFinish(
                numCourses,
                prerequisites
        )
);
""".formatted(
                    parseInteger(parts[0]),
                    javaIntMatrix(parts[1])
            );
        }

        if ("trap".equals(functionName)) {

            return """
int[] height = %s;
System.out.println(
        trap(height)
);
""".formatted(
                    javaIntArray(input)
            );
        }

        throw unsupportedFunction(functionName);
    }

    private String buildPythonCode(
            CodingProblem problem,
            String userCode,
            String input
    ) {

        return """
%s

%s
""".formatted(
                userCode,
                buildPythonInvocation(
                        problem,
                        input
                )
        );
    }

    private String buildPythonInvocation(
            CodingProblem problem,
            String input
    ) {

        String functionName =
                problem.getFunctionName();

        if ("twoSum".equals(functionName)) {

            String[] parts =
                    splitTwoSumInput(input);

            return """
nums = %s
target = %s
print(twoSum(nums, target))
""".formatted(
                    pythonIntArray(parts[0]),
                    parseInteger(parts[1])
            );
        }

        if ("isValid".equals(functionName)) {

            return """
s = %s
print(isValid(s))
""".formatted(
                    pythonString(
                            extractStringValue(input)
                    )
            );
        }

        if ("maxProfit".equals(functionName)) {

            return """
prices = %s
print(maxProfit(prices))
""".formatted(
                    pythonIntArray(input)
            );
        }

        if ("search".equals(functionName)) {

            String[] parts =
                    splitTwoSumInput(input);

            return """
nums = %s
target = %s
print(search(nums, target))
""".formatted(
                    pythonIntArray(parts[0]),
                    parseInteger(parts[1])
            );
        }

        if ("lengthOfLongestSubstring".equals(functionName)) {

            return """
s = %s
print(lengthOfLongestSubstring(s))
""".formatted(
                    pythonString(
                            extractStringValue(input)
                    )
            );
        }

        if ("productExceptSelf".equals(functionName)) {

            return """
nums = %s
print(productExceptSelf(nums))
""".formatted(
                    pythonIntArray(input)
            );
        }

        if ("merge".equals(functionName)) {

            return """
intervals = %s
print(merge(intervals))
""".formatted(
                    pythonMatrix(input)
            );
        }

        if ("numIslands".equals(functionName)) {

            return """
grid = %s
print(numIslands(grid))
""".formatted(
                    pythonCharMatrix(input)
            );
        }

        if ("canFinish".equals(functionName)) {

            String[] parts =
                    splitCourseScheduleInput(input);

            return """
numCourses = %s
prerequisites = %s
print(canFinish(numCourses, prerequisites))
""".formatted(
                    parseInteger(parts[0]),
                    pythonMatrix(parts[1])
            );
        }

        if ("trap".equals(functionName)) {

            return """
height = %s
print(trap(height))
""".formatted(
                    pythonIntArray(input)
            );
        }

        throw unsupportedFunction(functionName);
    }

    private String buildKotlinCode(
            CodingProblem problem,
            String userCode,
            String input
    ) {

        return """
%s

fun main() {
%s
}
""".formatted(
                userCode,
                indent(
                        buildKotlinInvocation(
                                problem,
                                input
                        ),
                        4
                )
        );
    }

    private String buildKotlinInvocation(
            CodingProblem problem,
            String input
    ) {

        String functionName =
                problem.getFunctionName();

        if ("twoSum".equals(functionName)) {

            String[] parts =
                    splitTwoSumInput(input);

            return """
val nums = %s
val target = %s
println(
    twoSum(nums, target).contentToString()
)
""".formatted(
                    kotlinIntArray(parts[0]),
                    parseInteger(parts[1])
            );
        }

        if ("isValid".equals(functionName)) {

            return """
val s = %s
println(isValid(s))
""".formatted(
                    kotlinString(
                            extractStringValue(input)
                    )
            );
        }

        if ("maxProfit".equals(functionName)) {

            return """
val prices = %s
println(maxProfit(prices))
""".formatted(
                    kotlinIntArray(input)
            );
        }

        if ("search".equals(functionName)) {

            String[] parts =
                    splitTwoSumInput(input);

            return """
val nums = %s
val target = %s
println(search(nums, target))
""".formatted(
                    kotlinIntArray(parts[0]),
                    parseInteger(parts[1])
            );
        }

        if ("lengthOfLongestSubstring".equals(functionName)) {

            return """
val s = %s
println(lengthOfLongestSubstring(s))
""".formatted(
                    kotlinString(
                            extractStringValue(input)
                    )
            );
        }

        if ("productExceptSelf".equals(functionName)) {

            return """
val nums = %s
println(
    productExceptSelf(nums).contentToString()
)
""".formatted(
                    kotlinIntArray(input)
            );
        }

        if ("merge".equals(functionName)) {

            return """
val intervals = %s
println(
    merge(intervals)
        .joinToString(
            prefix = "[",
            postfix = "]"
        ) {
            it.contentToString()
        }
)
""".formatted(
                    kotlinMatrix(input)
            );
        }

        if ("numIslands".equals(functionName)) {

            return """
val grid = %s
println(numIslands(grid))
""".formatted(
                    kotlinCharMatrix(input)
            );
        }

        if ("canFinish".equals(functionName)) {

            String[] parts =
                    splitCourseScheduleInput(input);

            return """
val numCourses = %s
val prerequisites = %s
println(
    canFinish(
        numCourses,
        prerequisites
    )
)
""".formatted(
                    parseInteger(parts[0]),
                    kotlinMatrix(parts[1])
            );
        }

        if ("trap".equals(functionName)) {

            return """
val height = %s
println(trap(height))
""".formatted(
                    kotlinIntArray(input)
            );
        }

        throw unsupportedFunction(functionName);
    }

    private String buildGoCode(
            CodingProblem problem,
            String userCode,
            String input
    ) {

        return """
package main

import "fmt"

%s

func main() {
%s
}
""".formatted(
                userCode,
                indent(
                        buildGoInvocation(
                                problem,
                                input
                        ),
                        4
                )
        );
    }

    private String buildGoInvocation(
            CodingProblem problem,
            String input
    ) {

        String functionName =
                problem.getFunctionName();

        if ("twoSum".equals(functionName)) {

            String[] parts =
                    splitTwoSumInput(input);

            return """
nums := %s
target := %s
fmt.Println(twoSum(nums, target))
""".formatted(
                    goIntArray(parts[0]),
                    parseInteger(parts[1])
            );
        }

        if ("isValid".equals(functionName)) {

            return """
s := %s
fmt.Println(isValid(s))
""".formatted(
                    goString(
                            extractStringValue(input)
                    )
            );
        }

        if ("maxProfit".equals(functionName)) {

            return """
prices := %s
fmt.Println(maxProfit(prices))
""".formatted(
                    goIntArray(input)
            );
        }

        if ("search".equals(functionName)) {

            String[] parts =
                    splitTwoSumInput(input);

            return """
nums := %s
target := %s
fmt.Println(search(nums, target))
""".formatted(
                    goIntArray(parts[0]),
                    parseInteger(parts[1])
            );
        }

        if ("lengthOfLongestSubstring".equals(functionName)) {

            return """
s := %s
fmt.Println(lengthOfLongestSubstring(s))
""".formatted(
                    goString(
                            extractStringValue(input)
                    )
            );
        }

        if ("productExceptSelf".equals(functionName)) {

            return """
nums := %s
fmt.Println(productExceptSelf(nums))
""".formatted(
                    goIntArray(input)
            );
        }

        if ("merge".equals(functionName)) {

            return """
intervals := %s
fmt.Println(merge(intervals))
""".formatted(
                    goMatrix(input)
            );
        }

        if ("numIslands".equals(functionName)) {

            return """
grid := %s
fmt.Println(numIslands(grid))
""".formatted(
                    goCharMatrix(input)
            );
        }

        if ("canFinish".equals(functionName)) {

            String[] parts =
                    splitCourseScheduleInput(input);

            return """
numCourses := %s
prerequisites := %s
fmt.Println(
    canFinish(
        numCourses,
        prerequisites
    )
)
""".formatted(
                    parseInteger(parts[0]),
                    goMatrix(parts[1])
            );
        }

        if ("trap".equals(functionName)) {

            return """
height := %s
fmt.Println(trap(height))
""".formatted(
                    goIntArray(input)
            );
        }

        throw unsupportedFunction(functionName);
    }

    private String buildRustCode(
            CodingProblem problem,
            String userCode,
            String input
    ) {

        return """
%s

fn main() {
%s
}
""".formatted(
                userCode,
                indent(
                        buildRustInvocation(
                                problem,
                                input
                        ),
                        4
                )
        );
    }

    private String buildRustInvocation(
            CodingProblem problem,
            String input
    ) {

        String functionName =
                problem.getFunctionName();

        if ("twoSum".equals(functionName)) {

            String[] parts =
                    splitTwoSumInput(input);

            return """
let nums = %s;
let target = %s;
println!("{:?}", twoSum(nums, target));
""".formatted(
                    rustIntArray(parts[0]),
                    parseInteger(parts[1])
            );
        }

        if ("isValid".equals(functionName)) {

            return """
let s = %s.to_string();
println!("{}", isValid(s));
""".formatted(
                    rustString(
                            extractStringValue(input)
                    )
            );
        }

        if ("maxProfit".equals(functionName)) {

            return """
let prices = %s;
println!("{}", maxProfit(prices));
""".formatted(
                    rustIntArray(input)
            );
        }

        if ("search".equals(functionName)) {

            String[] parts =
                    splitTwoSumInput(input);

            return """
let nums = %s;
let target = %s;
println!("{}", search(nums, target));
""".formatted(
                    rustIntArray(parts[0]),
                    parseInteger(parts[1])
            );
        }

        if ("lengthOfLongestSubstring".equals(functionName)) {

            return """
let s = %s.to_string();
println!("{}", lengthOfLongestSubstring(s));
""".formatted(
                    rustString(
                            extractStringValue(input)
                    )
            );
        }

        if ("productExceptSelf".equals(functionName)) {

            return """
let nums = %s;
println!("{:?}", productExceptSelf(nums));
""".formatted(
                    rustIntArray(input)
            );
        }

        if ("merge".equals(functionName)) {

            return """
let intervals = %s;
println!("{:?}", merge(intervals));
""".formatted(
                    rustMatrix(input)
            );
        }

        if ("numIslands".equals(functionName)) {

            return """
let grid = %s;
println!("{}", numIslands(grid));
""".formatted(
                    rustCharMatrix(input)
            );
        }

        if ("canFinish".equals(functionName)) {

            String[] parts =
                    splitCourseScheduleInput(input);

            return """
let numCourses = %s;
let prerequisites = %s;
println!(
    "{}",
    canFinish(
        numCourses,
        prerequisites
    )
);
""".formatted(
                    parseInteger(parts[0]),
                    rustMatrix(parts[1])
            );
        }

        if ("trap".equals(functionName)) {

            return """
let height = %s;
println!("{}", trap(height));
""".formatted(
                    rustIntArray(input)
            );
        }

        throw unsupportedFunction(functionName);
    }

    private IllegalArgumentException unsupportedFunction(
            String functionName
    ) {

        return new IllegalArgumentException(
                "Execution wrapper is not configured for function: "
                        + (
                        functionName == null ||
                                functionName.isBlank()
                                ? "unknown"
                                : functionName
                )
        );
    }

    private String javaString(
            String value
    ) {

        return quote(
                extractStringValue(value)
        );
    }

    private String pythonString(
            String value
    ) {

        return quotePython(
                extractStringValue(value)
        );
    }

    private String kotlinString(
            String value
    ) {

        return quote(
                extractStringValue(value)
        );
    }

    private String goString(
            String value
    ) {

        return quote(
                extractStringValue(value)
        );
    }

    private String rustString(
            String value
    ) {

        return quote(
                extractStringValue(value)
        );
    }

    private String quote(
            String value
    ) {

        String clean =
                value == null
                        ? ""
                        : value;

        return "\"" +
                clean
                        .replace("\\", "\\\\")
                        .replace("\"", "\\\"")
                        .replace("\n", "\\n")
                        .replace("\r", "\\r")
                        .replace("\t", "\\t") +
                "\"";
    }

    private String quotePython(
            String value
    ) {

        String clean =
                value == null
                        ? ""
                        : value;

        return "\"" +
                clean
                        .replace("\\", "\\\\")
                        .replace("\"", "\\\"")
                        .replace("\n", "\\n")
                        .replace("\r", "\\r")
                        .replace("\t", "\\t") +
                "\"";
    }

    private String extractStringValue(
            String value
    ) {

        if (value == null) {
            return "";
        }

        String clean =
                value.trim();

        if (
                clean.length() >= 2 &&
                clean.startsWith("\"") &&
                clean.endsWith("\"")
        ) {

            return clean.substring(
                    1,
                    clean.length() - 1
            );
        }

        return clean;
    }

    private String javaIntArray(
            String value
    ) {

        return "new int[]{" +
                cleanArray(value) +
                "}";
    }

    private String pythonIntArray(
            String value
    ) {

        return "[" +
                cleanArray(value) +
                "]";
    }

    private String kotlinIntArray(
            String value
    ) {

        return "intArrayOf(" +
                cleanArray(value) +
                ")";
    }

    private String goIntArray(
            String value
    ) {

        return "[]int{" +
                cleanArray(value) +
                "}";
    }

    private String rustIntArray(
            String value
    ) {

        return "vec![" +
                cleanArray(value) +
                "]";
    }

    private String cleanArray(
            String value
    ) {

        if (value == null || value.isBlank()) {
            return "";
        }

        String clean =
                value.trim();

        int equalsIndex =
                clean.indexOf("=");

        if (equalsIndex >= 0) {

            clean =
                    clean.substring(
                            equalsIndex + 1
                    ).trim();
        }

        if (
                clean.startsWith("[") &&
                clean.endsWith("]")
        ) {

            clean =
                    clean.substring(
                            1,
                            clean.length() - 1
                    );
        }

        clean = clean.trim();
        if (!clean.contains(",") && clean.matches(".*\\s+.*")) {
            clean = String.join(", ", clean.split("\\s+"));
        }

        return clean.trim();
    }

    private String javaIntMatrix(
            String value
    ) {

        List<List<Integer>> matrix =
                parseIntegerMatrix(
                        cleanMatrix(value)
                );

        if (matrix.isEmpty()) {
            return "new int[][]{}";
        }

        StringBuilder builder =
                new StringBuilder(
                        "new int[][]{"
                );

        for (
                int i = 0;
                i < matrix.size();
                i++
        ) {

            if (i > 0) {
                builder.append(",");
            }

            builder.append(
                    "new int[]{"
            );

            List<Integer> row =
                    matrix.get(i);

            for (
                    int j = 0;
                    j < row.size();
                    j++
            ) {

                if (j > 0) {
                    builder.append(",");
                }

                builder.append(
                        row.get(j)
                );
            }

            builder.append("}");
        }

        return builder
                .append("}")
                .toString();
    }

    private String pythonMatrix(
            String value
    ) {

        String clean =
                cleanMatrix(value);

        if (clean.isBlank()) {
            return "[]";
        }

        return clean
                .replace("{", "[")
                .replace("}", "]");
    }

    private String kotlinMatrix(
            String value
    ) {

        List<List<Integer>> matrix =
                parseIntegerMatrix(
                        cleanMatrix(value)
                );

        StringBuilder builder =
                new StringBuilder(
                        "arrayOf("
                );

        for (
                int i = 0;
                i < matrix.size();
                i++
        ) {

            if (i > 0) {
                builder.append(",");
            }

            builder.append(
                    "intArrayOf("
            );

            List<Integer> row =
                    matrix.get(i);

            for (
                    int j = 0;
                    j < row.size();
                    j++
            ) {

                if (j > 0) {
                    builder.append(",");
                }

                builder.append(
                        row.get(j)
                );
            }

            builder.append(")");
        }

        return builder
                .append(")")
                .toString();
    }

    private String goMatrix(
            String value
    ) {

        List<List<Integer>> matrix =
                parseIntegerMatrix(
                        cleanMatrix(value)
                );

        StringBuilder builder =
                new StringBuilder(
                        "[][]int{"
                );

        for (
                int i = 0;
                i < matrix.size();
                i++
        ) {

            if (i > 0) {
                builder.append(",");
            }

            builder.append(
                    "[]int{"
            );

            List<Integer> row =
                    matrix.get(i);

            for (
                    int j = 0;
                    j < row.size();
                    j++
            ) {

                if (j > 0) {
                    builder.append(",");
                }

                builder.append(
                        row.get(j)
                );
            }

            builder.append("}");
        }

        return builder
                .append("}")
                .toString();
    }

    private String rustMatrix(
            String value
    ) {

        List<List<Integer>> matrix =
                parseIntegerMatrix(
                        cleanMatrix(value)
                );

        StringBuilder builder =
                new StringBuilder(
                        "vec!["
                );

        for (
                int i = 0;
                i < matrix.size();
                i++
        ) {

            if (i > 0) {
                builder.append(",");
            }

            builder.append(
                    "vec!["
            );

            List<Integer> row =
                    matrix.get(i);

            for (
                    int j = 0;
                    j < row.size();
                    j++
            ) {

                if (j > 0) {
                    builder.append(",");
                }

                builder.append(
                        row.get(j)
                );
            }

            builder.append("]");
        }

        return builder
                .append("]")
                .toString();
    }

    private String javaCharMatrix(
            String value
    ) {

        List<List<String>> matrix =
                parseCharacterMatrix(
                        cleanMatrix(value)
                );

        StringBuilder builder =
                new StringBuilder(
                        "new char[][]{"
                );

        for (
                int i = 0;
                i < matrix.size();
                i++
        ) {

            if (i > 0) {
                builder.append(",");
            }

            builder.append(
                    "new char[]{"
            );

            List<String> row =
                    matrix.get(i);

            for (
                    int j = 0;
                    j < row.size();
                    j++
            ) {

                if (j > 0) {
                    builder.append(",");
                }

                builder.append("'")
                        .append(
                                escapeChar(
                                        row.get(j)
                                )
                        )
                        .append("'");
            }

            builder.append("}");
        }

        return builder
                .append("}")
                .toString();
    }

    private String pythonCharMatrix(
            String value
    ) {

        List<List<String>> matrix =
                parseCharacterMatrix(
                        cleanMatrix(value)
                );

        StringBuilder builder =
                new StringBuilder("[");
        
        for (
                int i = 0;
                i < matrix.size();
                i++
        ) {

            if (i > 0) {
                builder.append(",");
            }

            builder.append("[");

            List<String> row =
                    matrix.get(i);

            for (
                    int j = 0;
                    j < row.size();
                    j++
            ) {

                if (j > 0) {
                    builder.append(",");
                }

                builder.append(
                        quotePython(
                                row.get(j)
                        )
                );
            }

            builder.append("]");
        }

        return builder
                .append("]")
                .toString();
    }

    private String kotlinCharMatrix(
            String value
    ) {

        List<List<String>> matrix =
                parseCharacterMatrix(
                        cleanMatrix(value)
                );

        StringBuilder builder =
                new StringBuilder(
                        "arrayOf("
                );

        for (
                int i = 0;
                i < matrix.size();
                i++
        ) {

            if (i > 0) {
                builder.append(",");
            }

            builder.append(
                    "charArrayOf("
            );

            List<String> row =
                    matrix.get(i);

            for (
                    int j = 0;
                    j < row.size();
                    j++
            ) {

                if (j > 0) {
                    builder.append(",");
                }

                builder.append("'")
                        .append(
                                escapeChar(
                                        row.get(j)
                                )
                        )
                        .append("'");
            }

            builder.append(")");
        }

        return builder
                .append(")")
                .toString();
    }

    private String goCharMatrix(
            String value
    ) {

        List<List<String>> matrix =
                parseCharacterMatrix(
                        cleanMatrix(value)
                );

        StringBuilder builder =
                new StringBuilder(
                        "[][]byte{"
                );

        for (
                int i = 0;
                i < matrix.size();
                i++
        ) {

            if (i > 0) {
                builder.append(",");
            }

            builder.append(
                    "[]byte{"
            );

            List<String> row =
                    matrix.get(i);

            for (
                    int j = 0;
                    j < row.size();
                    j++
            ) {

                if (j > 0) {
                    builder.append(",");
                }

                String character =
                        row.get(j);

                if (character.isEmpty()) {
                    builder.append("0");
                } else {
                    builder.append("'")
                            .append(
                                    escapeGoChar(
                                            character
                                    )
                            )
                            .append("'");
                }
            }

            builder.append("}");
        }

        return builder
                .append("}")
                .toString();
    }

    private String rustCharMatrix(
            String value
    ) {

        List<List<String>> matrix =
                parseCharacterMatrix(
                        cleanMatrix(value)
                );

        StringBuilder builder =
                new StringBuilder(
                        "vec!["
                );

        for (
                int i = 0;
                i < matrix.size();
                i++
        ) {

            if (i > 0) {
                builder.append(",");
            }

            builder.append("vec![");

            List<String> row =
                    matrix.get(i);

            for (
                    int j = 0;
                    j < row.size();
                    j++
            ) {

                if (j > 0) {
                    builder.append(",");
                }

                builder.append(
                        quote(
                                row.get(j)
                        )
                )
                .append(
                        ".chars().next().unwrap()"
                );
            }

            builder.append("]");
        }

        return builder
                .append("]")
                .toString();
    }

    private String cleanMatrix(
            String value
    ) {

        if (value == null || value.isBlank()) {
            return "";
        }

        String clean =
                value
                        .replace(
                                "\r\n",
                                "\n"
                        )
                        .replace(
                                "\r",
                                "\n"
                        )
                        .trim();

        int equalsIndex =
                clean.indexOf("=");

        if (equalsIndex >= 0) {

            String left =
                    clean.substring(
                            0,
                            equalsIndex
                    ).trim();

            String right =
                    clean.substring(
                            equalsIndex + 1
                    ).trim();

            String normalizedLeft =
                    left.toLowerCase();

            if (
                    normalizedLeft.contains("grid") ||
                    normalizedLeft.contains("interval") ||
                    normalizedLeft.contains("prerequisite") ||
                    normalizedLeft.contains("nums") ||
                    normalizedLeft.contains("array")
            ) {
                clean = right;
            }
        }

        return clean;
    }

    private List<List<Integer>> parseIntegerMatrix(
            String value
    ) {

        List<List<Integer>> result =
                new ArrayList<>();

        if (value == null || value.isBlank()) {
            return result;
        }

        String clean =
                value.trim();

        if (
                clean.startsWith("[") &&
                clean.endsWith("]")
        ) {
            clean =
                    clean.substring(
                            1,
                            clean.length() - 1
                    ).trim();
        }

        List<String> rows =
                splitTopLevel(
                        clean,
                        ','
                );

        for (String row : rows) {

            String normalized =
                    row.trim();

            if (
                    normalized.startsWith("[") &&
                    normalized.endsWith("]")
            ) {

                normalized =
                        normalized.substring(
                                1,
                                normalized.length() - 1
                        ).trim();
            }

            if (
                    normalized.startsWith("{") &&
                    normalized.endsWith("}")
            ) {

                normalized =
                        normalized.substring(
                                1,
                                normalized.length() - 1
                        ).trim();
            }

            if (normalized.isBlank()) {
                continue;
            }

            List<Integer> values =
                    new ArrayList<>();

            for (
                    String number :
                    normalized.split(",")
            ) {

                String cleanNumber =
                        number.trim();

                cleanNumber =
                        cleanNumber.replaceAll(
                                "[^0-9-]",
                                ""
                        );

                if (cleanNumber.isBlank()) {
                    continue;
                }

                try {

                    values.add(
                            Integer.parseInt(
                                    cleanNumber
                            )
                    );

                } catch (NumberFormatException ignored) {
                }
            }

            if (!values.isEmpty()) {
                result.add(values);
            }
        }

        return result;
    }

    private List<List<String>> parseCharacterMatrix(
            String value
    ) {

        List<List<String>> result =
                new ArrayList<>();

        if (value == null || value.isBlank()) {
            return result;
        }

        String clean =
                value.trim();

        if (
                clean.startsWith("[") &&
                clean.endsWith("]")
        ) {

            clean =
                    clean.substring(
                            1,
                            clean.length() - 1
                    ).trim();
        }

        List<String> rows =
                splitTopLevel(
                        clean,
                        ','
                );

        for (String row : rows) {

            String normalized =
                    row.trim();

            if (
                    normalized.startsWith("[") &&
                    normalized.endsWith("]")
            ) {

                normalized =
                        normalized.substring(
                                1,
                                normalized.length() - 1
                        ).trim();
            }

            if (normalized.isBlank()) {
                continue;
            }

            List<String> values =
                    new ArrayList<>();

            for (
                    String valuePart :
                    normalized.split(",")
            ) {

                String character =
                        valuePart
                                .trim()
                                .replace(
                                        "\"",
                                        ""
                                )
                                .replace(
                                        "'",
                                        ""
                                );

                if (!character.isBlank()) {

                    values.add(
                            character
                    );
                }
            }

            if (!values.isEmpty()) {
                result.add(values);
            }
        }

        return result;
    }

    private List<String> splitTopLevel(
            String value,
            char separator
    ) {

        List<String> result =
                new ArrayList<>();

        StringBuilder current =
                new StringBuilder();

        int depth = 0;
        boolean quoted = false;
        char quote = 0;

        for (
                int i = 0;
                i < value.length();
                i++
        ) {

            char character =
                    value.charAt(i);

            if (
                    character == '"' ||
                    character == '\''
            ) {

                if (!quoted) {

                    quoted = true;
                    quote = character;

                } else if (quote == character) {

                    quoted = false;
                }
            }

            if (!quoted) {

                if (
                        character == '[' ||
                        character == '{'
                ) {
                    depth++;
                }

                if (
                        character == ']' ||
                        character == '}'
                ) {
                    depth--;
                }
            }

            if (
                    character == separator &&
                    depth == 0 &&
                    !quoted
            ) {

                result.add(
                        current.toString()
                );

                current.setLength(0);

            } else {

                current.append(character);
            }
        }

        if (current.length() > 0) {

            result.add(
                    current.toString()
            );
        }

        return result;
    }

    private String[] splitTwoSumInput(
            String input
    ) {

        if (input == null || input.isBlank()) {

            return new String[]{
                    "[]",
                    "0"
            };
        }

        String clean =
                input.trim();

        if (clean.contains("\n")) {
            String[] lines = clean.split("\\r?\\n", 2);
            return new String[]{
                    lines[0].trim(),
                    lines.length > 1 ? lines[1].trim() : "0"
            };
        }

        int equalsIndex =
                clean.indexOf("=");

        if (equalsIndex >= 0) {

            int commaAfterArray =
                    findTopLevelComma(
                            clean,
                            equalsIndex + 1
                    );

            if (commaAfterArray >= 0) {

                String first =
                        clean.substring(
                                equalsIndex + 1,
                                commaAfterArray
                        ).trim();

                String remainder =
                        clean.substring(
                                commaAfterArray + 1
                        ).trim();

                int targetEquals =
                        remainder.indexOf("=");

                if (targetEquals >= 0) {

                    remainder =
                            remainder.substring(
                                    targetEquals + 1
                            ).trim();
                }

                return new String[]{
                        first,
                        remainder
                };
            }
        }

        List<String> parts =
                splitTopLevel(
                        clean,
                        ','
                );

        if (parts.size() >= 2) {

            return new String[]{
                    parts.get(0),
                    parts.get(1)
            };
        }

        return new String[]{
                clean,
                "0"
        };
    }

    private int findTopLevelComma(
            String value,
            int startIndex
    ) {

        int depth = 0;
        boolean quoted = false;
        char quote = 0;

        for (
                int i = Math.max(0, startIndex);
                i < value.length();
                i++
        ) {

            char character =
                    value.charAt(i);

            if (
                    character == '"' ||
                    character == '\''
            ) {

                if (!quoted) {

                    quoted = true;
                    quote = character;

                } else if (quote == character) {

                    quoted = false;
                }
            }

            if (!quoted) {

                if (
                        character == '[' ||
                        character == '{'
                ) {
                    depth++;
                }

                if (
                        character == ']' ||
                        character == '}'
                ) {
                    depth--;
                }

                if (
                        character == ',' &&
                        depth == 0
                ) {
                    return i;
                }
            }
        }

        return -1;
    }

    private String[] splitCourseScheduleInput(
            String input
    ) {

        if (input == null || input.isBlank()) {

            return new String[]{
                    "0",
                    "[]"
            };
        }

        String clean =
                input.trim();

        int firstEquals =
                clean.indexOf("=");

        if (firstEquals < 0) {

            return new String[]{
                    "0",
                    clean
            };
        }

        String remainder =
                clean.substring(
                        firstEquals + 1
                ).trim();

        int comma =
                findTopLevelComma(
                        remainder,
                        0
                );

        if (comma >= 0) {

            String first =
                    remainder.substring(
                            0,
                            comma
                    ).trim();

            String rest =
                    remainder.substring(
                            comma + 1
                    ).trim();

            int secondEquals =
                    rest.indexOf("=");

            if (secondEquals >= 0) {

                rest =
                        rest.substring(
                                secondEquals + 1
                        ).trim();
            }

            return new String[]{
                    first,
                    rest
            };
        }

        return new String[]{
                remainder,
                "[]"
        };
    }

    private int parseInteger(
            String value
    ) {

        if (value == null || value.isBlank()) {
            return 0;
        }

        String clean =
                value.trim()
                        .replaceAll(
                                "[^0-9-]",
                                ""
                        );

        if (
                clean.isBlank() ||
                "-".equals(clean)
        ) {
            return 0;
        }

        try {

            return Integer.parseInt(clean);

        } catch (NumberFormatException exception) {

            return 0;
        }
    }

    private String requiredValue(
            Map<String, Object> configuration,
            String key,
            String language
    ) {

        String value =
                stringValue(
                        configuration.get(key)
                );

        if (value == null) {

            throw new IllegalArgumentException(
                    key +
                            " is not configured for: "
                            + language
            );
        }

        return value;
    }

    private boolean matches(
            String requested,
            String value
    ) {

        if (
                requested == null ||
                requested.isBlank() ||
                value == null ||
                value.isBlank()
        ) {
            return false;
        }

        return requested.equalsIgnoreCase(
                value.trim()
        );
    }

    private String stringValue(
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

    private Map<String, Object> convertMap(
            Map<?, ?> source
    ) {

        Map<String, Object> result =
                new LinkedHashMap<>();

        for (
                Map.Entry<?, ?> entry :
                source.entrySet()
        ) {

            if (entry.getKey() != null) {

                result.put(
                        entry.getKey().toString(),
                        entry.getValue()
                );
            }
        }

        return result;
    }

    private String indent(
            String value,
            int spaces
    ) {

        if (value == null || value.isBlank()) {
            return "";
        }

        String prefix =
                " ".repeat(spaces);

        return prefix +
                value.replace(
                        "\n",
                        "\n" + prefix
                );
    }

    private String escapeChar(
            String value
    ) {

        if (value == null || value.isEmpty()) {
            return "";
        }

        return value
                .replace(
                        "\\",
                        "\\\\"
                )
                .replace(
                        "'",
                        "\\'"
                );
    }

    private String escapeGoChar(
            String value
    ) {

        if (value == null || value.isEmpty()) {
            return "";
        }

        String character =
                value.substring(
                        0,
                        1
                );

        if ("\\".equals(character)) {
            return "\\\\";
        }

        if ("'".equals(character)) {
            return "\\'";
        }

        if ("\n".equals(character)) {
            return "\\n";
        }

        if ("\r".equals(character)) {
            return "\\r";
        }

        if ("\t".equals(character)) {
            return "\\t";
        }

        return character;
    }
}