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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
@Transactional
public class CodeExecutionServiceImpl
        implements CodeExecutionService {

    private static final String PISTON_URL =
            "http://localhost:2000/api/v2/execute";

    private final CodingProblemRepository codingProblemRepository;

    private final CodingTestCaseRepository codingTestCaseRepository;

    private final FunctionExecutionWrapperService wrapperService;

    private final HttpClient httpClient;

    private final ObjectMapper objectMapper;

    public CodeExecutionServiceImpl(
            CodingProblemRepository codingProblemRepository,
            CodingTestCaseRepository codingTestCaseRepository,
            FunctionExecutionWrapperService wrapperService
    ) {
        this.codingProblemRepository =
                codingProblemRepository;

        this.codingTestCaseRepository =
                codingTestCaseRepository;

        this.wrapperService =
                wrapperService;

        this.httpClient =
                HttpClient.newHttpClient();

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

        if (request.getLanguage() == null ||
                request.getLanguage().isBlank()) {

            return errorResponse(
                    "Programming language is required.",
                    "Missing programming language."
            );
        }

        if (request.getCode() == null ||
                request.getCode().isBlank()) {

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

        String runtimeLanguage;

        String runtimeVersion;

        String fileName;

        try {

            runtimeLanguage =
                    wrapperService.getRuntimeLanguage(
                            problem,
                            request.getLanguage()
                    );

            runtimeVersion =
                    wrapperService.getRuntimeVersion(
                            problem,
                            request.getLanguage()
                    );

            fileName =
                    wrapperService.getFileName(
                            problem,
                            request.getLanguage()
                    );

        } catch (Exception exception) {

            return errorResponse(
                    exception.getMessage(),
                    "Language configuration error."
            );
        }

        if (runtimeLanguage == null ||
                runtimeLanguage.isBlank()) {

            return errorResponse(
                    "Runtime language is not configured.",
                    "Missing runtime language."
            );
        }

        if (runtimeVersion == null ||
                runtimeVersion.isBlank()) {

            return errorResponse(
                    "Runtime version is not configured.",
                    "Missing runtime version."
            );
        }

        List<CodingTestCase> testCases =
                codingTestCaseRepository
                        .findByProblemAndActiveTrueOrderByTestCaseNumberAsc(
                                problem
                        );

        if (testCases == null ||
                testCases.isEmpty()) {

            return errorResponse(
                    "No active test cases found.",
                    "The selected problem has no test cases."
            );
        }

        List<CodeExecutionTestCaseResponse> results =
                new ArrayList<>();

        int passedTests = 0;

        int failedTests = 0;

        long totalRuntime = 0L;

        for (CodingTestCase testCase :
                testCases) {

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
        }

        boolean allPassed =
                passedTests == testCases.size() &&
                failedTests == 0;

        boolean compileError =
                results.stream()
                        .anyMatch(
                                result ->
                                        "COMPILE_ERROR"
                                                .equals(
                                                        result.getStatus()
                                                )
                        );

        boolean runtimeError =
                results.stream()
                        .anyMatch(
                                result ->
                                        "RUNTIME_ERROR"
                                                .equals(
                                                        result.getStatus()
                                                )
                        );

        String status;

        if (allPassed) {

            status = "ACCEPTED";

        } else if (compileError) {

            status = "COMPILE_ERROR";

        } else if (runtimeError) {

            status = "RUNTIME_ERROR";

        } else {

            status = "WRONG_ANSWER";
        }

        String message =
                allPassed
                        ? "All test cases passed."
                        : passedTests +
                          " of " +
                          testCases.size() +
                          " test cases passed.";

        return CodeExecutionResponse.builder()
                .status(status)
                .passed(allPassed)
                .totalTests(testCases.size())
                .passedTests(passedTests)
                .failedTests(failedTests)
                .runtime(totalRuntime)
                .output("")
                .expectedOutput("")
                .error("")
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

        long start =
                System.currentTimeMillis();

        try {

            String executableCode =
                    wrapperService.buildExecutableCode(
                            problem,
                            request.getLanguage(),
                            request.getCode(),
                            testCase.getInput()
                    );

            Map<String, Object> file =
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
                            List.of(file)
                    );

            String json =
                    objectMapper.writeValueAsString(
                            payload
                    );

            HttpRequest httpRequest =
                    HttpRequest.newBuilder()
                            .uri(
                                    URI.create(
                                            PISTON_URL
                                    )
                            )
                            .header(
                                    "Content-Type",
                                    "application/json"
                            )
                            .POST(
                                    HttpRequest.BodyPublishers
                                            .ofString(
                                                    json
                                            )
                            )
                            .build();

            HttpResponse<String> response =
                    httpClient.send(
                            httpRequest,
                            HttpResponse.BodyHandlers
                                    .ofString()
                    );

            long elapsed =
                    System.currentTimeMillis()
                            - start;

            if (response.statusCode() < 200 ||
                    response.statusCode() >= 300) {

                return buildFailedTestCase(
                        testCase,
                        "ERROR",
                        "Execution server returned HTTP " +
                                response.statusCode(),
                        elapsed
                );
            }

            JsonNode root =
                    objectMapper.readTree(
                            response.body()
                    );

            JsonNode compile =
                    root.path("compile");

            JsonNode run =
                    root.path("run");

            int compileCode =
                    compile.path(
                            "code"
                    ).asInt(0);

            String compileError =
                    compile.path(
                            "stderr"
                    ).asText("");

            if (compileCode != 0 ||
                    !compileError.isBlank()) {

                return CodeExecutionTestCaseResponse
                        .builder()
                        .testCaseNumber(
                                testCase.getTestCaseNumber()
                        )
                        .passed(false)
                        .input(
                                testCase.isHidden()
                                        ? null
                                        : testCase.getInput()
                        )
                        .expectedOutput(
                                testCase.isHidden()
                                        ? null
                                        : testCase.getExpectedOutput()
                        )
                        .actualOutput(null)
                        .error(
                                compileError
                        )
                        .runtime(elapsed)
                        .status(
                                "COMPILE_ERROR"
                        )
                        .build();
            }

            int runCode =
                    run.path(
                            "code"
                    ).asInt(0);

            String stdout =
                    run.path(
                            "stdout"
                    ).asText("");

            String stderr =
                    run.path(
                            "stderr"
                    ).asText("");

            long runtime =
                    run.path(
                            "cpu_time"
                    ).asLong(
                            elapsed
                    );

            String actualOutput =
                    normalizeOutput(
                            stdout
                    );

            String expectedOutput =
                    normalizeOutput(
                            testCase.getExpectedOutput()
                    );

            if (runCode != 0) {

                return CodeExecutionTestCaseResponse
                        .builder()
                        .testCaseNumber(
                                testCase.getTestCaseNumber()
                        )
                        .passed(false)
                        .input(
                                testCase.isHidden()
                                        ? null
                                        : testCase.getInput()
                        )
                        .expectedOutput(
                                testCase.isHidden()
                                        ? null
                                        : testCase.getExpectedOutput()
                        )
                        .actualOutput(null)
                        .error(stderr)
                        .runtime(runtime)
                        .status(
                                "RUNTIME_ERROR"
                        )
                        .build();
            }

            boolean passed =
                    stderr.isBlank() &&
                    actualOutput.equals(
                            expectedOutput
                    );

            return CodeExecutionTestCaseResponse
                    .builder()
                    .testCaseNumber(
                            testCase.getTestCaseNumber()
                    )
                    .passed(passed)
                    .input(
                            testCase.isHidden()
                                    ? null
                                    : testCase.getInput()
                    )
                    .expectedOutput(
                            testCase.isHidden()
                                    ? null
                                    : testCase.getExpectedOutput()
                    )
                    .actualOutput(
                            testCase.isHidden()
                                    ? null
                                    : actualOutput
                    )
                    .error(
                            stderr
                    )
                    .runtime(runtime)
                    .status(
                            passed
                                    ? "PASSED"
                                    : "FAILED"
                    )
                    .build();

        } catch (Exception exception) {

            long elapsed =
                    System.currentTimeMillis()
                            - start;

            return buildFailedTestCase(
                    testCase,
                    "ERROR",
                    exception.getMessage(),
                    elapsed
            );
        }
    }

    private CodeExecutionTestCaseResponse
    buildFailedTestCase(
            CodingTestCase testCase,
            String status,
            String error,
            long runtime
    ) {

        return CodeExecutionTestCaseResponse
                .builder()
                .testCaseNumber(
                        testCase.getTestCaseNumber()
                )
                .passed(false)
                .input(
                        testCase.isHidden()
                                ? null
                                : testCase.getInput()
                )
                .expectedOutput(
                        testCase.isHidden()
                                ? null
                                : testCase.getExpectedOutput()
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