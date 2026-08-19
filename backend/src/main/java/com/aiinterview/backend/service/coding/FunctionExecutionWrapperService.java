package com.aiinterview.backend.service.coding;

import com.aiinterview.backend.entity.CodingProblem;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
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

        return runtimeLanguage;
    }

    public String getRuntimeVersion(
            CodingProblem problem,
            String language
    ) {

        Map<String, Object> configuration =
                getLanguageConfiguration(
                        problem,
                        language
                );

        String runtimeVersion =
                stringValue(
                        configuration.get("runtimeVersion")
                );

        if (runtimeVersion == null) {

            throw new IllegalArgumentException(
                    "Runtime version is not configured for: "
                            + language
            );
        }

        return runtimeVersion;
    }

    public String getFileName(
            CodingProblem problem,
            String language
    ) {

        Map<String, Object> configuration =
                getLanguageConfiguration(
                        problem,
                        language
                );

        String fileName =
                stringValue(
                        configuration.get("fileName")
                );

        if (fileName == null) {

            throw new IllegalArgumentException(
                    "File name is not configured for: "
                            + language
            );
        }

        return fileName;
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

        if (language == null ||
                language.isBlank()) {

            throw new IllegalArgumentException(
                    "Programming language cannot be empty."
            );
        }

        String code =
                userCode == null
                        ? ""
                        : userCode.trim();

        if (code.isBlank()) {

            throw new IllegalArgumentException(
                    "Code cannot be empty."
            );
        }

        String testInput =
                input == null
                        ? ""
                        : input;

        String normalizedLanguage =
                language
                        .trim()
                        .toLowerCase();

        return switch (normalizedLanguage) {

            case "java" ->
                    buildJavaCode(
                            problem,
                            code,
                            testInput
                    );

            case "python", "python3" ->
                    buildPythonCode(
                            problem,
                            code,
                            testInput
                    );

            case "kotlin" ->
                    buildKotlinCode(
                            problem,
                            code,
                            testInput
                    );

            case "go" ->
                    buildGoCode(
                            problem,
                            code,
                            testInput
                    );

            case "rust" ->
                    buildRustCode(
                            problem,
                            code,
                            testInput
                    );

            default ->
                    buildGenericCode(
                            problem,
                            language,
                            code,
                            testInput
                    );
        };
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

        if (language == null ||
                language.isBlank()) {

            throw new IllegalArgumentException(
                    "Programming language cannot be empty."
            );
        }

        String configurations =
                problem.getLanguageConfigurations();

        if (configurations == null ||
                configurations.isBlank()) {

            throw new IllegalArgumentException(
                    "Language configurations are not available for this problem."
            );
        }

        Map<String, Object> languageConfigurations;

        try {

            languageConfigurations =
                    objectMapper.readValue(
                            configurations,
                            new TypeReference<
                                    LinkedHashMap<String, Object>
                            >() {
                            }
                    );

        } catch (Exception exception) {

            throw new IllegalArgumentException(
                    "Invalid language configuration.",
                    exception
            );
        }

        Map<String, Object> configuration =
                findConfiguration(
                        languageConfigurations,
                        language.trim()
                );

        if (configuration == null) {

            throw new IllegalArgumentException(
                    "Language is not supported for this problem: "
                            + language
            );
        }

        return configuration;
    }

    private Map<String, Object> findConfiguration(
            Map<String, Object> configurations,
            String requestedLanguage
    ) {

        String normalized =
                requestedLanguage
                        .trim()
                        .toLowerCase();

        Object direct =
                configurations.get(
                        normalized
                );

        if (direct instanceof Map<?, ?> map) {

            return convertMap(map);
        }

        for (
                Map.Entry<String, Object> entry :
                configurations.entrySet()
        ) {

            if (!(entry.getValue()
                    instanceof Map<?, ?> rawMap)) {

                continue;
            }

            Map<String, Object> configuration =
                    convertMap(rawMap);

            String key =
                    entry.getKey();

            String displayName =
                    stringValue(
                            configuration.get(
                                    "displayName"
                            )
                    );

            String runtimeLanguage =
                    stringValue(
                            configuration.get(
                                    "runtimeLanguage"
                            )
                    );

            if (
                    matches(
                            normalized,
                            key
                    ) ||
                    matches(
                            normalized,
                            displayName
                    ) ||
                    matches(
                            normalized,
                            runtimeLanguage
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

    private String buildJavaCode(
            CodingProblem problem,
            String userCode,
            String input
    ) {

        String functionName =
                problem.getFunctionName();

        if (functionName == null ||
                functionName.isBlank()) {

            return userCode;
        }

        String invocation =
                buildJavaInvocation(
                        problem,
                        input
                );

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
                userCode,
                invocation
        );
    }

    private String buildJavaInvocation(
            CodingProblem problem,
            String input
    ) {

        String functionName =
                problem.getFunctionName();

        String normalizedInput =
                input == null
                        ? ""
                        : input.trim();

        if ("twoSum".equals(functionName)) {

            return """
int[] nums = new int[]{2, 7, 11, 15};
int target = 9;
System.out.println(
        Arrays.toString(
                twoSum(nums, target)
        )
);
""";
        }

        if ("isValid".equals(functionName)) {

            return """
String s = %s;
System.out.println(isValid(s));
""".formatted(
                    javaString(
                            normalizedInput
                    )
            );
        }

        if ("maxProfit".equals(functionName)) {

            return """
int[] prices = %s;
System.out.println(maxProfit(prices));
""".formatted(
                    javaIntArray(
                            normalizedInput
                    )
            );
        }

        if ("search".equals(functionName)) {

            return """
int[] nums = new int[]{-1, 0, 3, 5, 9, 12};
int target = 9;
System.out.println(search(nums, target));
""";
        }

        if ("lengthOfLongestSubstring".equals(functionName)) {

            return """
String s = %s;
System.out.println(lengthOfLongestSubstring(s));
""".formatted(
                    javaString(
                            normalizedInput
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
                    javaIntArray(
                            normalizedInput
                    )
            );
        }

        if ("trap".equals(functionName)) {

            return """
int[] height = %s;
System.out.println(trap(height));
""".formatted(
                    javaIntArray(
                            normalizedInput
                    )
            );
        }

        return "";
    }

    private String buildPythonCode(
            CodingProblem problem,
            String userCode,
            String input
    ) {

        String functionName =
                problem.getFunctionName();

        String invocation =
                buildPythonInvocation(
                        problem,
                        input
                );

        return """
%s

%s
""".formatted(
                userCode,
                invocation
        );
    }

    private String buildPythonInvocation(
            CodingProblem problem,
            String input
    ) {

        String functionName =
                problem.getFunctionName();

        String normalizedInput =
                input == null
                        ? ""
                        : input.trim();

        if ("twoSum".equals(functionName)) {

            return """
nums = [2, 7, 11, 15]
target = 9
print(twoSum(nums, target))
""";
        }

        if ("isValid".equals(functionName)) {

            return """
s = %s
print(isValid(s))
""".formatted(
                    pythonString(
                            normalizedInput
                    )
            );
        }

        if ("maxProfit".equals(functionName)) {

            return """
prices = %s
print(maxProfit(prices))
""".formatted(
                    pythonIntArray(
                            normalizedInput
                    )
            );
        }

        if ("search".equals(functionName)) {

            return """
nums = [-1, 0, 3, 5, 9, 12]
target = 9
print(search(nums, target))
""";
        }

        if (
                "lengthOfLongestSubstring"
                        .equals(functionName)
        ) {

            return """
s = %s
print(lengthOfLongestSubstring(s))
""".formatted(
                    pythonString(
                            normalizedInput
                    )
            );
        }

        if ("productExceptSelf".equals(functionName)) {

            return """
nums = %s
print(productExceptSelf(nums))
""".formatted(
                    pythonIntArray(
                            normalizedInput
                    )
            );
        }

        if ("trap".equals(functionName)) {

            return """
height = %s
print(trap(height))
""".formatted(
                    pythonIntArray(
                            normalizedInput
                    )
            );
        }

        return "";
    }

    private String buildKotlinCode(
            CodingProblem problem,
            String userCode,
            String input
    ) {

        String invocation =
                buildKotlinInvocation(
                        problem,
                        input
                );

        return """
import java.io.BufferedReader
import java.io.InputStreamReader

%s

fun main() {
%s
}
""".formatted(
                userCode,
                invocation
        );
    }

    private String buildKotlinInvocation(
            CodingProblem problem,
            String input
    ) {

        String functionName =
                problem.getFunctionName();

        if ("isValid".equals(functionName)) {

            return """
    val s = %s
    println(isValid(s))
""".formatted(
                    kotlinString(
                            input
                    )
            );
        }

        if ("maxProfit".equals(functionName)) {

            return """
    val prices = %s
    println(maxProfit(prices))
""".formatted(
                    kotlinIntArray(
                            input
                    )
            );
        }

        if ("search".equals(functionName)) {

            return """
    val nums = intArrayOf(-1, 0, 3, 5, 9, 12)
    val target = 9
    println(search(nums, target))
""";
        }

        if (
                "lengthOfLongestSubstring"
                        .equals(functionName)
        ) {

            return """
    val s = %s
    println(lengthOfLongestSubstring(s))
""".formatted(
                    kotlinString(
                            input
                    )
            );
        }

        if ("productExceptSelf".equals(functionName)) {

            return """
    val nums = %s
    println(productExceptSelf(nums).contentToString())
""".formatted(
                    kotlinIntArray(
                            input
                    )
            );
        }

        if ("trap".equals(functionName)) {

            return """
    val height = %s
    println(trap(height))
""".formatted(
                    kotlinIntArray(
                            input
                    )
            );
        }

        return "";
    }

    private String buildGoCode(
            CodingProblem problem,
            String userCode,
            String input
    ) {

        String invocation =
                buildGoInvocation(
                        problem,
                        input
                );

        return """
package main

import "fmt"

%s

func main() {
%s
}
""".formatted(
                userCode,
                invocation
        );
    }

    private String buildGoInvocation(
            CodingProblem problem,
            String input
    ) {

        String functionName =
                problem.getFunctionName();

        if ("isValid".equals(functionName)) {

            return """
    s := %s
    fmt.Println(isValid(s))
""".formatted(
                    goString(input)
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

            return """
    nums := []int{-1, 0, 3, 5, 9, 12}
    target := 9
    fmt.Println(search(nums, target))
""";
        }

        if (
                "lengthOfLongestSubstring"
                        .equals(functionName)
        ) {

            return """
    s := %s
    fmt.Println(lengthOfLongestSubstring(s))
""".formatted(
                    goString(input)
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

        if ("trap".equals(functionName)) {

            return """
    height := %s
    fmt.Println(trap(height))
""".formatted(
                    goIntArray(input)
            );
        }

        return "";
    }

    private String buildRustCode(
            CodingProblem problem,
            String userCode,
            String input
    ) {

        String invocation =
                buildRustInvocation(
                        problem,
                        input
                );

        return """
%s

fn main() {
%s
}
""".formatted(
                userCode,
                invocation
        );
    }

    private String buildRustInvocation(
            CodingProblem problem,
            String input
    ) {

        String functionName =
                problem.getFunctionName();

        if ("isValid".equals(functionName)) {

            return """
    let s = %s.to_string();
    println!("{}", is_valid(s));
""".formatted(
                    rustString(input)
            );
        }

        if ("maxProfit".equals(functionName)) {

            return """
    let prices = %s;
    println!("{}", max_profit(prices));
""".formatted(
                    rustIntArray(input)
            );
        }

        if ("search".equals(functionName)) {

            return """
    let nums = vec![-1, 0, 3, 5, 9, 12];
    let target = 9;
    println!("{}", search(nums, target));
""";
        }

        if (
                "lengthOfLongestSubstring"
                        .equals(functionName)
        ) {

            return """
    let s = %s.to_string();
    println!("{}", length_of_longest_substring(s));
""".formatted(
                    rustString(input)
            );
        }

        if ("productExceptSelf".equals(functionName)) {

            return """
    let nums = %s;
    println!("{:?}", product_except_self(nums));
""".formatted(
                    rustIntArray(input)
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

        return "";
    }

    private String buildGenericCode(
            CodingProblem problem,
            String language,
            String userCode,
            String input
    ) {

        Map<String, Object> configuration =
                getLanguageConfiguration(
                        problem,
                        language
                );

        String template =
                stringValue(
                        configuration.get(
                                "executionTemplate"
                        )
                );

        if (template == null ||
                template.isBlank()) {

            return userCode;
        }

        return template
                .replace(
                        "{{USER_CODE}}",
                        userCode
                )
                .replace(
                        "{{TEST_INPUT}}",
                        input == null
                                ? ""
                                : input
                );
    }

    private String javaString(
            String value
    ) {

        String clean =
                extractStringValue(value);

        return "\"" +
                clean
                        .replace(
                                "\\",
                                "\\\\"
                        )
                        .replace(
                                "\"",
                                "\\\""
                        )
                        .replace(
                                "\n",
                                "\\n"
                        ) +
                "\"";
    }

    private String pythonString(
            String value
    ) {

        String clean =
                extractStringValue(value);

        return "\"" +
                clean
                        .replace(
                                "\\",
                                "\\\\"
                        )
                        .replace(
                                "\"",
                                "\\\""
                        )
                        .replace(
                                "\n",
                                "\\n"
                        ) +
                "\"";
    }

    private String kotlinString(
            String value
    ) {

        return javaString(value);
    }

    private String goString(
            String value
    ) {

        String clean =
                extractStringValue(value);

        return "\"" +
                clean
                        .replace(
                                "\\",
                                "\\\\"
                        )
                        .replace(
                                "\"",
                                "\\\""
                        )
                        .replace(
                                "\n",
                                "\\n"
                        ) +
                "\"";
    }

    private String rustString(
            String value
    ) {

        String clean =
                extractStringValue(value);

        return "\"" +
                clean
                        .replace(
                                "\\",
                                "\\\\"
                        )
                        .replace(
                                "\"",
                                "\\\""
                        )
                        .replace(
                                "\n",
                                "\\n"
                        ) +
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
                clean.startsWith("\"") &&
                clean.endsWith("\"") &&
                clean.length() >= 2
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

        return normalizeArray(
                value,
                "new int[]{",
                "}"
        );
    }

    private String pythonIntArray(
            String value
    ) {

        String clean =
                value == null
                        ? ""
                        : value.trim();

        if (clean.isBlank()) {
            return "[]";
        }

        if (
                clean.startsWith("[") &&
                clean.endsWith("]")
        ) {

            return clean;
        }

        return "[" + clean + "]";
    }

    private String kotlinIntArray(
            String value
    ) {

        String clean =
                value == null
                        ? ""
                        : value.trim();

        if (clean.isBlank()) {
            return "intArrayOf()";
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

        return "intArrayOf(" +
                clean +
                ")";
    }

    private String goIntArray(
            String value
    ) {

        String clean =
                value == null
                        ? ""
                        : value.trim();

        if (clean.isBlank()) {
            return "[]int{}";
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

        return "[]int{" +
                clean +
                "}";
    }

    private String rustIntArray(
            String value
    ) {

        String clean =
                value == null
                        ? ""
                        : value.trim();

        if (clean.isBlank()) {
            return "vec![]";
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

        return "vec![" +
                clean +
                "]";
    }

    private String normalizeArray(
            String value,
            String prefix,
            String suffix
    ) {

        String clean =
                value == null
                        ? ""
                        : value.trim();

        if (clean.isBlank()) {

            return prefix +
                    suffix;
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

        return prefix +
                clean +
                suffix;
    }

    private boolean matches(
            String requested,
            String value
    ) {

        if (value == null ||
                value.isBlank()) {

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
}