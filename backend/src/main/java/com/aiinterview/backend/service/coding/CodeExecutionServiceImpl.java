package com.aiinterview.backend.service.coding;

import com.aiinterview.backend.dto.coding.CodeExecutionRequest;
import com.aiinterview.backend.dto.coding.CodeExecutionResponse;
import com.aiinterview.backend.entity.CodingProblem;
import com.aiinterview.backend.repository.CodingProblemRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Map;

@Service
@Transactional
public class CodeExecutionServiceImpl implements CodeExecutionService {

    private final CodingProblemRepository codingProblemRepository;
    private final ObjectMapper objectMapper;
    private final HttpClient httpClient;

    @Value("${app.piston.url}")
    private String pistonUrl;

   public CodeExecutionServiceImpl(
        CodingProblemRepository codingProblemRepository
) {
    this.codingProblemRepository = codingProblemRepository;
    this.objectMapper = new ObjectMapper();
    this.httpClient = HttpClient.newBuilder()
            .build();
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
                        .findById(request.getProblemId())
                        .orElse(null);

        if (problem == null) {
            return errorResponse(
                    "Coding problem not found.",
                    "Invalid problem ID."
            );
        }

        String language = normalizeLanguage(
                request.getLanguage()
        );

        String version = getVersion(language);

        if (version == null) {
            return errorResponse(
                    "This programming language is not supported yet.",
                    "Unsupported language: " + request.getLanguage()
            );
        }

        String fileName = getFileName(language);

        try {

            Map<String, Object> file = Map.of(
                    "name", fileName,
                    "content", request.getCode()
            );

            Map<String, Object> payload = Map.of(
                    "language", language,
                    "version", version,
                    "files", new Object[]{file}
            );

            String requestBody =
                    objectMapper.writeValueAsString(payload);

            HttpRequest httpRequest =
                    HttpRequest.newBuilder()
                            .uri(URI.create(pistonUrl))
                            .header(
                                    "Content-Type",
                                    "application/json"
                            )
                            .POST(
                                    HttpRequest.BodyPublishers.ofString(
                                            requestBody
                                    )
                            )
                            .build();

            long startTime =
                    System.currentTimeMillis();

            HttpResponse<String> response =
                    httpClient.send(
                            httpRequest,
                            HttpResponse.BodyHandlers.ofString()
                    );

            long requestRuntime =
                    System.currentTimeMillis() - startTime;

            if (response.statusCode() < 200 ||
                    response.statusCode() >= 300) {

                return errorResponse(
                        "Code execution service returned an error.",
                        "Piston HTTP status: "
                                + response.statusCode()
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

            String compileStderr =
                    compile.path("stderr")
                            .asText("");

            String compileOutput =
                    compile.path("output")
                            .asText("");

            int compileCode =
                    compile.path("code")
                            .asInt(0);

            if (compileCode != 0) {

                String error =
                        !compileStderr.isBlank()
                                ? compileStderr
                                : compileOutput;

                return CodeExecutionResponse.builder()
                        .status("COMPILE_ERROR")
                        .passed(false)
                        .totalTests(1)
                        .passedTests(0)
                        .failedTests(1)
                        .runtime(requestRuntime)
                        .output("")
                        .expectedOutput(
                                problem.getOutputExample()
                        )
                        .message(
                                "Code compilation failed."
                        )
                        .error(error)
                        .build();
            }

            String stdout =
                    run.path("stdout")
                            .asText("");

            String stderr =
                    run.path("stderr")
                            .asText("");

            int runCode =
                    run.path("code")
                            .asInt(0);

            long runtime =
                    run.path("wall_time")
                            .asLong(requestRuntime);

            if (runCode != 0) {

                String error =
                        !stderr.isBlank()
                                ? stderr
                                : run.path("message")
                                        .asText(
                                                "Runtime execution failed."
                                        );

                return CodeExecutionResponse.builder()
                        .status("RUNTIME_ERROR")
                        .passed(false)
                        .totalTests(1)
                        .passedTests(0)
                        .failedTests(1)
                        .runtime(runtime)
                        .output(stdout)
                        .expectedOutput(
                                problem.getOutputExample()
                        )
                        .message(
                                "Code execution failed."
                        )
                        .error(error)
                        .build();
            }

            String normalizedOutput =
                    normalizeOutput(stdout);

            String expectedOutput =
                    normalizeOutput(
                            problem.getOutputExample()
                    );

            boolean passed =
                    normalizedOutput.equals(
                            expectedOutput
                    );

            return CodeExecutionResponse.builder()
                    .status(
                            passed
                                    ? "ACCEPTED"
                                    : "WRONG_ANSWER"
                    )
                    .passed(passed)
                    .totalTests(1)
                    .passedTests(
                            passed ? 1 : 0
                    )
                    .failedTests(
                            passed ? 0 : 1
                    )
                    .runtime(runtime)
                    .output(stdout)
                    .expectedOutput(
                            problem.getOutputExample()
                    )
                    .message(
                            passed
                                    ? "All test cases passed successfully."
                                    : "Output does not match the expected output."
                    )
                    .error("")
                    .build();

        } catch (Exception exception) {

            return errorResponse(
                    "Unable to execute code.",
                    exception.getMessage()
            );
        }
    }

    private String normalizeLanguage(
            String language
    ) {

        String value =
                language.trim()
                        .toLowerCase();

        return switch (value) {
            case "cpp", "c++", "g++" -> "c++";
            case "javascript", "js", "node" -> "javascript";
            case "python", "python3", "py" -> "python";
            case "java" -> "java";
            case "c", "gcc" -> "c";
            default -> value;
        };
    }

    private String getVersion(
            String language
    ) {

        return switch (language) {
            case "java" -> "15.0.2";
            case "c++", "c" -> "10.2.0";
            case "python" -> "3.12.0";
            case "javascript" -> "20.11.1";
            default -> null;
        };
    }

    private String getFileName(
            String language
    ) {

        return switch (language) {
            case "java" -> "Main.java";
            case "c++" -> "main.cpp";
            case "c" -> "main.c";
            case "python" -> "main.py";
            case "javascript" -> "main.js";
            default -> "main.txt";
        };
    }

    private String normalizeOutput(
            String output
    ) {

        if (output == null) {
            return "";
        }

        return output
                .replace("\r\n", "\n")
                .replace("\r", "\n")
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
                .message(message)
                .error(
                        error == null
                                ? ""
                                : error
                )
                .build();
    }
}