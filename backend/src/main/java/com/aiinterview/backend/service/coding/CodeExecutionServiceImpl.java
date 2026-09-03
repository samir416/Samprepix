package com.aiinterview.backend.service.coding;

import com.aiinterview.backend.dto.coding.CodeExecutionRequest;
import com.aiinterview.backend.dto.coding.CodeExecutionResponse;
import com.aiinterview.backend.dto.coding.CodeExecutionTestCaseResponse;
import com.aiinterview.backend.entity.CodingProblem;
import com.aiinterview.backend.entity.CodingTestCase;
import com.aiinterview.backend.repository.CodingProblemRepository;
import com.aiinterview.backend.repository.CodingTestCaseRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
@Transactional
public class CodeExecutionServiceImpl
        implements CodeExecutionService {

    private final CodingProblemRepository codingProblemRepository;
    private final CodingTestCaseRepository codingTestCaseRepository;
    private final FunctionExecutionWrapperService wrapperService;
    private final PistonRuntimeService pistonRuntimeService;
    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;
    private final String pistonUrl;

    public CodeExecutionServiceImpl(
            CodingProblemRepository codingProblemRepository,
            CodingTestCaseRepository codingTestCaseRepository,
            FunctionExecutionWrapperService wrapperService,
            PistonRuntimeService pistonRuntimeService,
            @Value("${app.piston.url:http://localhost:2000/api/v2/execute}")
            String pistonUrl
    ) {

        this.codingProblemRepository =
                codingProblemRepository;

        this.codingTestCaseRepository =
                codingTestCaseRepository;

        this.wrapperService =
                wrapperService;

        this.pistonRuntimeService =
                pistonRuntimeService;

        this.pistonUrl =
                pistonUrl;

        this.httpClient =
                HttpClient.newBuilder()
                        .version(HttpClient.Version.HTTP_1_1)
                        .connectTimeout(
                                Duration.ofSeconds(10)
                        )
                        .build();

        this.objectMapper =
                new ObjectMapper();
    }

    @Override
    @Transactional(readOnly = true)
    public CodeExecutionResponse execute(
            CodeExecutionRequest request
    ) {

        if (request == null) {

            return errorResponse(
                    "Execution request cannot be null.",
                    "Invalid execution request."
            );
        }

        if (request.getProblemId() == null) {

            return errorResponse(
                    "Problem ID is required.",
                    "Missing problem ID."
            );
        }

        if (
                request.getLanguage() == null ||
                request.getLanguage().isBlank()
        ) {

            return errorResponse(
                    "Programming language is required.",
                    "Missing programming language."
            );
        }

        if (
                request.getCode() == null ||
                request.getCode().isBlank()
        ) {

            return errorResponse(
                    "Code cannot be empty.",
                    "Empty code submission."
            );
        }

        CodingProblem problem =
                codingProblemRepository
                        .findById(
                                request.getProblemId()
                        )
                        .orElse(null);

        if (problem == null) {

            return errorResponse(
                    "Coding problem not found.",
                    "Invalid problem ID."
            );
        }

        if (!problem.isActive()) {

            return errorResponse(
                    "This coding problem is no longer active.",
                    "Inactive coding problem."
            );
        }

        List<CodingTestCase> testCases =
                codingTestCaseRepository
                        .findByProblemAndActiveTrueOrderByTestCaseNumberAsc(
                                problem
                        );

        if (
                testCases == null ||
                testCases.isEmpty()
        ) {

            return errorResponse(
                    "No active test cases are available for this problem.",
                    "Missing test cases."
            );
        }

        String runtimeLanguage;
        String runtimeVersion;
        String fileName;

        try {

            runtimeLanguage =
                    wrapperService.getRuntimeLanguage(
                            problem,
                            request.getLanguage()
                    );

            String configuredRuntimeVersion =
                    wrapperService.getRuntimeVersion(
                            problem,
                            request.getLanguage()
                    );

            PistonRuntimeService.PistonRuntime runtime =
                    pistonRuntimeService.findRuntime(
                            runtimeLanguage,
                            configuredRuntimeVersion
                    );

            runtimeLanguage = runtime.getLanguage();
            runtimeVersion = runtime.getVersion();

            fileName =
                    wrapperService.getFileName(
                            problem,
                            request.getLanguage()
                    );

        } catch (Exception exception) {

            return errorResponse(
                    safeMessage(
                            exception,
                            "Language configuration is invalid."
                    ),
                    "Language configuration error."
            );
        }

        if (
                runtimeLanguage == null ||
                runtimeLanguage.isBlank()
        ) {

            return errorResponse(
                    "The selected language is not configured for this problem.",
                    "Missing runtime language."
            );
        }

        if (
                fileName == null ||
                fileName.isBlank()
        ) {

            return errorResponse(
                    "The selected language filename is not configured.",
                    "Missing source filename."
            );
        }

        List<CodeExecutionTestCaseResponse> results =
                new ArrayList<>();

        int passedTests = 0;
        int failedTests = 0;
        long totalRuntime = 0L;

        for (
                CodingTestCase testCase :
                testCases
        ) {

            CodeExecutionTestCaseResponse result =
                    executeTestCase(
                            problem,
                            request,
                            runtimeLanguage,
                            runtimeVersion,
                            fileName,
                            testCase
                    );

            results.add(result);

            if (result.isPassed()) {
                passedTests++;
            } else {
                failedTests++;
            }

            if (result.getRuntime() != null) {

                totalRuntime +=
                        result.getRuntime();
            }

            if (
                    "COMPILE_ERROR".equals(
                            result.getStatus()
                    ) ||
                    "EXECUTION_ERROR".equals(
                            result.getStatus()
                    ) ||
                    "TIME_LIMIT_EXCEEDED".equals(
                            result.getStatus()
                    )
            ) {

                break;
            }
        }

        boolean allPassed =
                !results.isEmpty() &&
                passedTests == testCases.size();

        String status =
                determineOverallStatus(
                        results,
                        allPassed
                );

        String message;

        if (allPassed) {

            message =
                    "All test cases passed.";

        } else {

            message =
                    passedTests +
                            " of " +
                            testCases.size() +
                            " test cases passed.";
        }

        return CodeExecutionResponse.builder()
                .status(status)
                .passed(allPassed)
                .totalTests(
                        testCases.size()
                )
                .passedTests(
                        passedTests
                )
                .failedTests(
                        failedTests
                )
                .runtime(
                        totalRuntime
                )
                .output(
                        getLastOutput(
                                results
                        )
                )
                .expectedOutput(
                        getLastExpectedOutput(
                                results
                        )
                )
                .error(
                        getLastError(
                                results
                        )
                )
                .message(message)
                .testCases(results)
                .build();
    }

    private CodeExecutionTestCaseResponse executeTestCase(
            CodingProblem problem,
            CodeExecutionRequest request,
            String runtimeLanguage,
            String runtimeVersion,
            String fileName,
            CodingTestCase testCase
    ) {

        long startTime =
                System.currentTimeMillis();

        try {

            String executableCode =
                    wrapperService.buildExecutableCode(
                            problem,
                            request.getLanguage(),
                            request.getCode(),
                            testCase.getInput()
                    );

            Map<String, Object> sourceFile =
                    Map.of(
                            "name",
                            fileName,
                            "content",
                            executableCode
                    );

            Map<String, Object> payload =
                    Map.of(
                            "language",
                            runtimeLanguage,
                            "version",
                            runtimeVersion,
                            "files",
                            List.of(sourceFile),
                            "stdin",
                            testCase.getInput() != null ? testCase.getInput() : ""
                    );

            String requestBody =
                    objectMapper.writeValueAsString(
                            payload
                    );

            HttpRequest httpRequest =
                    HttpRequest.newBuilder()
                            .version(HttpClient.Version.HTTP_1_1)
                            .uri(
                                    URI.create(
                                            pistonUrl
                                    )
                            )
                            .timeout(
                                    Duration.ofSeconds(30)
                            )
                            .header(
                                    "Content-Type",
                                    "application/json"
                            )
                            .header(
                                    "Accept",
                                    "application/json"
                            )
                            .POST(
                                    HttpRequest.BodyPublishers
                                            .ofString(
                                                    requestBody
                                            )
                            )
                            .build();

            HttpResponse<String> response =
                    httpClient.send(
                            httpRequest,
                            HttpResponse.BodyHandlers.ofString()
                    );

            long requestRuntime =
                    System.currentTimeMillis()
                            - startTime;

            if (
                    response.statusCode() < 200 ||
                    response.statusCode() >= 300
            ) {

                return failedTestCase(
                        testCase,
                        "EXECUTION_ERROR",
                        "Execution server returned HTTP "
                                + response.statusCode() + ": " +
                                compactExecutionServerError(response.body()),
                        requestRuntime
                );
            }

            if (
                    response.body() == null ||
                    response.body().isBlank()
            ) {

                return failedTestCase(
                        testCase,
                        "EXECUTION_ERROR",
                        "Execution server returned an empty response.",
                        requestRuntime
                );
            }

            JsonNode root =
                    objectMapper.readTree(
                            response.body()
                    );

            if (
                    root == null ||
                    !root.isObject()
            ) {

                return failedTestCase(
                        testCase,
                        "EXECUTION_ERROR",
                        "Execution server returned an invalid response.",
                        requestRuntime
                );
            }

            JsonNode compileNode =
                    root.path("compile");

            JsonNode runNode =
                    root.path("run");

            if (
                    !compileNode.isMissingNode() &&
                    !compileNode.isNull()
            ) {

                int compileCode =
                        compileNode
                                .path("code")
                                .asInt(0);

                String compileStdout =
                        compileNode
                                .path("stdout")
                                .asText("");

                String compileStderr =
                        compileNode
                                .path("stderr")
                                .asText("");

                if (compileCode != 0) {

                    return CodeExecutionTestCaseResponse
                            .builder()
                            .testCaseNumber(
                                    testCase
                                            .getTestCaseNumber()
                            )
                            .passed(false)
                            .input(
                                    visibleValue(
                                            testCase,
                                            testCase.getInput()
                                    )
                            )
                            .expectedOutput(
                                    visibleValue(
                                            testCase,
                                            testCase
                                                    .getExpectedOutput()
                                    )
                            )
                            .actualOutput(null)
                            .error(
                                    firstNonBlank(
                                            compileStderr,
                                            compileStdout,
                                            "Compilation failed."
                                    )
                            )
                            .runtime(
                                    requestRuntime
                            )
                            .status(
                                    "COMPILE_ERROR"
                            )
                            .build();
                }
            }

            if (
                    runNode.isMissingNode() ||
                    runNode.isNull()
            ) {

                return failedTestCase(
                        testCase,
                        "EXECUTION_ERROR",
                        "Execution server did not return a run result.",
                        requestRuntime
                );
            }

            int runCode =
                    runNode
                            .path("code")
                            .asInt(0);

            String stdout =
                    runNode
                            .path("stdout")
                            .asText("");

            String stderr =
                    runNode
                            .path("stderr")
                            .asText("");

            long runtime =
                    runNode
                            .path("cpu_time")
                            .asLong(
                                    requestRuntime
                            );

            if (runtime <= 0) {
                runtime = requestRuntime;
            }

            if (runCode != 0) {

                String error =
                        firstNonBlank(
                                stderr,
                                stdout,
                                "Program execution failed."
                        );

                return CodeExecutionTestCaseResponse
                        .builder()
                        .testCaseNumber(
                                testCase
                                        .getTestCaseNumber()
                        )
                        .passed(false)
                        .input(
                                visibleValue(
                                        testCase,
                                        testCase.getInput()
                                )
                        )
                        .expectedOutput(
                                visibleValue(
                                        testCase,
                                        testCase
                                                .getExpectedOutput()
                                )
                        )
                        .actualOutput(null)
                        .error(error)
                        .runtime(runtime)
                        .status("RUNTIME_ERROR")
                        .build();
            }

            String actualOutput = normalizeOutput(stdout);

            String expectedOutput = normalizeOutput(
                    testCase.getExpectedOutput()
            );

            boolean passed = outputsMatch(actualOutput, expectedOutput);

            return CodeExecutionTestCaseResponse
                    .builder()
                    .testCaseNumber(
                            testCase
                                    .getTestCaseNumber()
                    )
                    .passed(passed)
                    .input(
                            visibleValue(
                                    testCase,
                                    testCase.getInput()
                            )
                    )
                    .expectedOutput(
                            visibleValue(
                                    testCase,
                                    testCase
                                            .getExpectedOutput()
                            )
                    )
                    .actualOutput(
                            visibleValue(
                                    testCase,
                                    actualOutput
                            )
                    )
                    .error(
                            stderr == null
                                    ? ""
                                    : stderr
                    )
                    .runtime(runtime)
                    .status(
                            passed
                                    ? "PASSED"
                                    : "FAILED"
                    )
                    .build();

        } catch (
                java.net.http.HttpTimeoutException exception
        ) {

            return failedTestCase(
                    testCase,
                    "TIME_LIMIT_EXCEEDED",
                    "Execution timed out.",
                    System.currentTimeMillis()
                            - startTime
            );

        } catch (InterruptedException exception) {

            Thread.currentThread()
                    .interrupt();

            return failedTestCase(
                    testCase,
                    "EXECUTION_ERROR",
                    "Execution was interrupted.",
                    System.currentTimeMillis()
                            - startTime
            );

        } catch (Exception exception) {

            return failedTestCase(
                    testCase,
                    "EXECUTION_ERROR",
                    safeMessage(
                            exception,
                            "Unable to execute code."
                    ),
                    System.currentTimeMillis()
                            - startTime
            );
        }
    }

    private String determineOverallStatus(
            List<CodeExecutionTestCaseResponse> results,
            boolean allPassed
    ) {

        if (allPassed) {
            return "ACCEPTED";
        }

        for (
                CodeExecutionTestCaseResponse result :
                results
        ) {

            if (
                    "COMPILE_ERROR".equals(
                            result.getStatus()
                    )
            ) {

                return "COMPILE_ERROR";
            }
        }

        for (
                CodeExecutionTestCaseResponse result :
                results
        ) {

            if (
                    "TIME_LIMIT_EXCEEDED".equals(
                            result.getStatus()
                    )
            ) {

                return "TIME_LIMIT_EXCEEDED";
            }
        }

        for (
                CodeExecutionTestCaseResponse result :
                results
        ) {

            if (
                    "RUNTIME_ERROR".equals(
                            result.getStatus()
                    )
            ) {

                return "RUNTIME_ERROR";
            }
        }

        for (
                CodeExecutionTestCaseResponse result :
                results
        ) {

            if (
                    "EXECUTION_ERROR".equals(
                            result.getStatus()
                    )
            ) {

                return "EXECUTION_ERROR";
            }
        }

        return "WRONG_ANSWER";
    }

    private CodeExecutionTestCaseResponse failedTestCase(
            CodingTestCase testCase,
            String status,
            String error,
            long runtime
    ) {

        return CodeExecutionTestCaseResponse
                .builder()
                .testCaseNumber(
                        testCase
                                .getTestCaseNumber()
                )
                .passed(false)
                .input(
                        visibleValue(
                                testCase,
                                testCase.getInput()
                        )
                )
                .expectedOutput(
                        visibleValue(
                                testCase,
                                testCase
                                        .getExpectedOutput()
                        )
                )
                .actualOutput(null)
                .error(
                        error == null
                                ? ""
                                : error
                )
                .runtime(runtime)
                .status(status)
                .build();
    }

    private String visibleValue(
            CodingTestCase testCase,
            String value
    ) {

        if (
                testCase == null ||
                testCase.isHidden()
        ) {

            return null;
        }

        return value;
    }

    private String normalizeOutput(
            String output
    ) {

        if (output == null) {
            return "";
        }

        return output
                .replace(
                        "\r\n",
                        "\n"
                )
                .replace(
                        "\r",
                        "\n"
                )
                .trim();
    }

    private boolean outputsMatch(String actualOutput, String expectedOutput) {

        return canonicalizeComparableOutput(actualOutput)
                .equals(canonicalizeComparableOutput(expectedOutput));
    }

    private String canonicalizeComparableOutput(String output) {

        return normalizeOutput(output)
                .replaceAll("[\\[\\](){},]", " ")
                .replaceAll("\\s+", " ")
                .trim();
    }

    private String firstNonBlank(
            String first,
            String second,
            String fallback
    ) {

        if (
                first != null &&
                !first.isBlank()
        ) {

            return first;
        }

        if (
                second != null &&
                !second.isBlank()
        ) {

            return second;
        }

        return fallback;
    }

    private String compactExecutionServerError(String responseBody) {

        if (responseBody == null || responseBody.isBlank()) {
            return "No error details returned.";
        }

        String compact = responseBody.replaceAll("\\s+", " ").trim();

        return compact.length() > 500
                ? compact.substring(0, 500) + "..."
                : compact;
    }

    private String getLastOutput(
            List<CodeExecutionTestCaseResponse> results
    ) {

        for (
                int index =
                        results.size() - 1;
                index >= 0;
                index--
        ) {

            String output =
                    results.get(index)
                            .getActualOutput();

            if (
                    output != null &&
                    !output.isBlank()
            ) {

                return output;
            }
        }

        return "";
    }

    private String getLastExpectedOutput(
            List<CodeExecutionTestCaseResponse> results
    ) {

        for (
                int index =
                        results.size() - 1;
                index >= 0;
                index--
        ) {

            String output =
                    results.get(index)
                            .getExpectedOutput();

            if (
                    output != null &&
                    !output.isBlank()
            ) {

                return output;
            }
        }

        return "";
    }

    private String getLastError(
            List<CodeExecutionTestCaseResponse> results
    ) {

        for (
                int index =
                        results.size() - 1;
                index >= 0;
                index--
        ) {

            String error =
                    results.get(index)
                            .getError();

            if (
                    error != null &&
                    !error.isBlank()
            ) {

                return error;
            }
        }

        return "";
    }

    private String safeMessage(
            Exception exception,
            String fallback
    ) {

        if (
                exception == null ||
                exception.getMessage() == null ||
                exception.getMessage().isBlank()
        ) {

            return fallback;
        }

        return exception.getMessage();
    }

    private CodeExecutionResponse errorResponse(
            String message,
            String error
    ) {

        return CodeExecutionResponse.builder()
                .status("ERROR")
                .passed(false)
                .totalTests(0)
                .passedTests(0)
                .failedTests(0)
                .runtime(0L)
                .output("")
                .expectedOutput("")
                .error(
                        error == null
                                ? ""
                                : error
                )
                .message(
                        message == null
                                ? "Execution failed."
                                : message
                )
                .testCases(
                        new ArrayList<>()
                )
                .build();
    }
}
