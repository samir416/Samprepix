package com.aiinterview.backend.controller;

import com.aiinterview.backend.dto.coding.CodeExecutionRequest;
import com.aiinterview.backend.dto.coding.CodeExecutionResponse;
import com.aiinterview.backend.entity.CodingProblem;
import com.aiinterview.backend.entity.GitHubConnection;
import com.aiinterview.backend.entity.User;
import com.aiinterview.backend.repository.CodingProblemRepository;
import com.aiinterview.backend.repository.GitHubConnectionRepository;
import com.aiinterview.backend.repository.UserRepository;
import com.aiinterview.backend.service.coding.CodeExecutionService;
import com.aiinterview.backend.service.coding.CodingProblemCompletionService;
import com.aiinterview.backend.service.coding.CodingProgressService;
import com.aiinterview.backend.service.coding.FunctionExecutionWrapperService;
import com.aiinterview.backend.service.coding.GitHubRepositoryService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/api/coding")
public class CodeExecutionController {

    private final CodeExecutionService codeExecutionService;
    private final CodingProblemRepository codingProblemRepository;
    private final UserRepository userRepository;
    private final GitHubConnectionRepository gitHubConnectionRepository;
    private final CodingProgressService codingProgressService;
    private final CodingProblemCompletionService codingProblemCompletionService;
    private final FunctionExecutionWrapperService wrapperService;
    private final GitHubRepositoryService gitHubRepositoryService;

    public CodeExecutionController(
            CodeExecutionService codeExecutionService,
            CodingProblemRepository codingProblemRepository,
            UserRepository userRepository,
            GitHubConnectionRepository gitHubConnectionRepository,
            CodingProgressService codingProgressService,
            CodingProblemCompletionService codingProblemCompletionService,
            FunctionExecutionWrapperService wrapperService,
            GitHubRepositoryService gitHubRepositoryService
    ) {
        this.codeExecutionService = codeExecutionService;
        this.codingProblemRepository = codingProblemRepository;
        this.userRepository = userRepository;
        this.gitHubConnectionRepository = gitHubConnectionRepository;
        this.codingProgressService = codingProgressService;
        this.codingProblemCompletionService =
                codingProblemCompletionService;
        this.wrapperService = wrapperService;
        this.gitHubRepositoryService = gitHubRepositoryService;
    }

    @PostMapping("/execute")
    public ResponseEntity<CodeExecutionResponse> executeCode(
            @RequestBody CodeExecutionRequest request
    ) {

        validateRequest(request);

        CodeExecutionResponse response =
                codeExecutionService.execute(request);

        return ResponseEntity.ok(response);
    }

    @PostMapping("/submit")
    public ResponseEntity<CodeExecutionResponse> submitCode(
            @RequestBody CodeExecutionRequest request,
            Authentication authentication
    ) {

        validateRequest(request);

        User user =
                getAuthenticatedUser(authentication);

        CodingProblem problem =
                codingProblemRepository
                        .findById(request.getProblemId())
                        .orElseThrow(
                                () ->
                                        new IllegalStateException(
                                                "Coding problem not found."
                                        )
                        );

        CodeExecutionResponse response =
                codeExecutionService.execute(request);

        boolean successful =
                response.isPassed();

        codingProgressService.updateSubmission(
                user,
                successful
        );

        codingProblemCompletionService.recordSubmission(
                        user,
                        problem,
                        request.getLanguage(),
                        request.getCode(),
                        successful
                );

        if (!successful) {
            return ResponseEntity.ok(response);
        }

        codingProgressService.saveCodeState(
                user,
                problem,
                request.getLanguage(),
                request.getCode()
        );

        codingProgressService.markProblemCompleted(
                user,
                problem
        );

        response.setMessage(
                "All test cases passed. Coding progress saved."
        );

        GitHubConnection connection =
                gitHubConnectionRepository
                        .findByUser(user)
                        .orElse(null);

        if (!isGitHubConnectionUsable(connection)) {
            return ResponseEntity.ok(response);
        }

        try {

            String fileName =
                    wrapperService.getFileName(
                            problem,
                            request.getLanguage()
                    );

            String solutionPath =
                    buildSolutionPath(
                            problem,
                            fileName
                    );

            gitHubRepositoryService.pushSolution(
                    user,
                    connection.getRepositoryUrl(),
                    solutionPath,
                    request.getCode(),
                    "Solve " + problem.getTitle() +
                            " in " + request.getLanguage()
            );

            response.setMessage(
                    "All test cases passed. Solution committed to GitHub."
            );

        } catch (Exception exception) {

            response.setMessage(
                    "All test cases passed and progress was saved, but the GitHub commit failed: "
                            + safeMessage(
                                    exception.getMessage()
                            )
            );
        }

        return ResponseEntity.ok(response);
    }

    private void validateRequest(
            CodeExecutionRequest request
    ) {

        if (request == null) {
            throw new IllegalArgumentException(
                    "Execution request cannot be null."
            );
        }

        if (request.getProblemId() == null) {
            throw new IllegalArgumentException(
                    "Problem ID is required."
            );
        }

        if (
                request.getLanguage() == null ||
                request.getLanguage().isBlank()
        ) {
            throw new IllegalArgumentException(
                    "Programming language is required."
            );
        }

        if (
                request.getCode() == null ||
                request.getCode().isBlank()
        ) {
            throw new IllegalArgumentException(
                    "Code cannot be empty."
            );
        }
    }

    private boolean isGitHubConnectionUsable(
            GitHubConnection connection
    ) {

        return connection != null &&
                connection.getAccessToken() != null &&
                !connection.getAccessToken().isBlank() &&
                connection.getRepositoryUrl() != null &&
                !connection.getRepositoryUrl().isBlank();
    }

    private String buildSolutionPath(
            CodingProblem problem,
            String fileName
    ) {

        String problemTitle =
                problem.getTitle() == null
                        ? "problem"
                        : problem.getTitle();

        String safeTitle =
                problemTitle
                        .trim()
                        .toLowerCase()
                        .replaceAll(
                                "[^a-z0-9]+",
                                "-"
                        )
                        .replaceAll(
                                "^-+|-+$",
                                ""
                        );

        if (safeTitle.isBlank()) {
            safeTitle = "problem";
        }

        String topic = problem.getTags() == null || problem.getTags().isEmpty()
                ? "general"
                : problem.getTags().get(0);

        String safeTopic = topic == null ? "general" : topic
                .trim()
                .toLowerCase()
                .replaceAll("[^a-z0-9]+", "-")
                .replaceAll("^-+|-+$", "");

        if (safeTopic.isBlank()) {
            safeTopic = "general";
        }

        String normalizedFileName = fileName == null || fileName.isBlank()
                ? "solution"
                : fileName.trim().replace("\\", "/");

        String extension = normalizedFileName.lastIndexOf('.') >= 0
                ? normalizedFileName.substring(normalizedFileName.lastIndexOf('.'))
                : "";

        return safeTopic + "/" + safeTitle + "/" +
                requestLanguageFolder(fileName) + "/solution" + extension;
    }

    private String requestLanguageFolder(String fileName) {
        String extension = fileName == null ? "" : fileName.toLowerCase();

        if (extension.endsWith(".py")) return "python";
        if (extension.endsWith(".kt")) return "kotlin";
        if (extension.endsWith(".go")) return "go";
        if (extension.endsWith(".rs")) return "rust";
        if (extension.endsWith(".js")) return "javascript";
        if (extension.endsWith(".ts")) return "typescript";
        if (extension.endsWith(".cpp")) return "cpp";
        if (extension.endsWith(".c")) return "c";
        if (extension.endsWith(".cs")) return "csharp";
        if (extension.endsWith(".php")) return "php";
        if (extension.endsWith(".rb")) return "ruby";
        if (extension.endsWith(".swift")) return "swift";
        if (extension.endsWith(".dart")) return "dart";
        return "java";
    }

    private User getAuthenticatedUser(
            Authentication authentication
    ) {

        if (
                authentication == null ||
                authentication.getName() == null ||
                authentication.getName().isBlank()
        ) {
            throw new IllegalStateException(
                    "Authenticated user not found."
            );
        }

        return userRepository
                .findByEmail(
                        authentication.getName()
                )
                .orElseThrow(
                        () ->
                                new IllegalStateException(
                                        "User account not found."
                                )
                );
    }

    private String safeMessage(
            String message
    ) {

        if (
                message == null ||
                message.isBlank()
        ) {
            return "Unknown GitHub error.";
        }

        return message;
    }
}