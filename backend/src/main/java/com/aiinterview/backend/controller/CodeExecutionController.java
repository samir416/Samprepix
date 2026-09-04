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
import com.aiinterview.backend.dto.coding.GitHubSyncResult;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.concurrent.ConcurrentHashMap;


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
    private final ConcurrentHashMap<String, Long> inFlightExecutions = new ConcurrentHashMap<>();

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
            @RequestBody CodeExecutionRequest request,
            Authentication authentication
    ) {

        validateRequest(request);

        String userKey = (authentication != null && authentication.getName() != null)
                ? authentication.getName() + ":" + request.getProblemId()
                : "anon:" + request.getProblemId();

        Long existingStart = inFlightExecutions.get(userKey);
        if (existingStart != null && (System.currentTimeMillis() - existingStart < 60_000L)) {
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).body(
                    CodeExecutionResponse.builder()
                            .status("EXECUTION_ERROR")
                            .passed(false)
                            .message("A code execution is already in progress for this problem. Please wait.")
                            .build()
            );
        }

        inFlightExecutions.put(userKey, System.currentTimeMillis());
        try {
            CodeExecutionResponse response =
                    codeExecutionService.execute(request, false);

            return ResponseEntity.ok(response);
        } finally {
            inFlightExecutions.remove(userKey);
        }
    }

    @PostMapping("/submit")
    public ResponseEntity<CodeExecutionResponse> submitCode(
            @RequestBody CodeExecutionRequest request,
            Authentication authentication
    ) {

        validateRequest(request);

        User user =
                getAuthenticatedUser(authentication);

        String userKey = user.getEmail() + ":" + request.getProblemId();

        Long existingStart = inFlightExecutions.get(userKey);
        if (existingStart != null && (System.currentTimeMillis() - existingStart < 60_000L)) {
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).body(
                    CodeExecutionResponse.builder()
                            .status("EXECUTION_ERROR")
                            .passed(false)
                            .message("A submission is already in progress for this problem. Please wait.")
                            .build()
            );
        }

        inFlightExecutions.put(userKey, System.currentTimeMillis());
        try {
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
                    codeExecutionService.execute(request, true);

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
                response.setGitHubSync(
                        GitHubSyncResult.builder()
                                .connected(false)
                                .synced(false)
                                .alreadySynced(false)
                                .message("GitHub not connected. Connect GitHub in Profile to enable automatic solution syncing.")
                                .build()
                );
                return ResponseEntity.ok(response);
            }

            try {
                GitHubRepositoryService.GitHubPushResult pushResult =
                        gitHubRepositoryService.syncSolution(
                                user,
                                connection.getRepositoryUrl(),
                                problem,
                                request.getLanguage(),
                                request.getCode()
                        );

                response.setGitHubSync(pushResult.toSyncResult());

                if (pushResult.isAlreadySynced()) {
                    response.setMessage(
                            "All test cases passed. Solution already synced to GitHub."
                    );
                } else if (pushResult.getSolutionNumber() != null && pushResult.getSolutionNumber() > 1) {
                    response.setMessage(
                            "All test cases passed. Solution " + pushResult.getSolutionNumber() + " synced to GitHub."
                    );
                } else {
                    response.setMessage(
                            "All test cases passed. Solution committed to GitHub."
                    );
                }

            } catch (Exception exception) {
                response.setGitHubSync(
                        GitHubSyncResult.builder()
                                .connected(true)
                                .synced(false)
                                .alreadySynced(false)
                                .error(safeMessage(exception.getMessage()))
                                .message("GitHub sync failed: " + safeMessage(exception.getMessage()))
                                .build()
                );

                response.setMessage(
                        "All test cases passed and progress was saved, but GitHub sync failed: "
                                + safeMessage(
                                        exception.getMessage()
                                )
                );
            }

            return ResponseEntity.ok(response);
        } finally {
            inFlightExecutions.remove(userKey);
        }
    }

    @PostMapping("/sync-github")
    public ResponseEntity<GitHubSyncResult> retryGitHubSync(
            @RequestBody CodeExecutionRequest request,
            Authentication authentication
    ) {

        validateRequest(request);

        User user = getAuthenticatedUser(authentication);

        CodingProblem problem = codingProblemRepository
                .findById(request.getProblemId())
                .orElseThrow(() -> new IllegalStateException("Coding problem not found."));

        GitHubConnection connection = gitHubConnectionRepository
                .findByUser(user)
                .orElse(null);

        if (!isGitHubConnectionUsable(connection)) {
            return ResponseEntity.badRequest().body(
                    GitHubSyncResult.builder()
                            .connected(false)
                            .synced(false)
                            .error("GitHub is not connected. Connect GitHub in Profile to enable automatic solution syncing.")
                            .message("GitHub is not connected.")
                            .build()
            );
        }

        try {
            GitHubRepositoryService.GitHubPushResult pushResult =
                    gitHubRepositoryService.syncSolution(
                            user,
                            connection.getRepositoryUrl(),
                            problem,
                            request.getLanguage(),
                            request.getCode()
                    );

            return ResponseEntity.ok(pushResult.toSyncResult());
        } catch (Exception exception) {
            return ResponseEntity.badRequest().body(
                    GitHubSyncResult.builder()
                            .connected(true)
                            .synced(false)
                            .error(safeMessage(exception.getMessage()))
                            .message("GitHub sync failed: " + safeMessage(exception.getMessage()))
                            .build()
            );
        }
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
        if (extension.endsWith(".rkt")) return "racket";
        if (extension.endsWith(".r")) return "r";
        if (extension.endsWith(".groovy")) return "groovy";
        if (extension.endsWith(".fsx") || extension.endsWith(".fs")) return "fsharp";
        if (extension.endsWith(".jl")) return "julia";
        if (extension.endsWith(".d")) return "d";
        if (extension.endsWith(".cob")) return "cobol";
        if (extension.endsWith(".ml")) return "ocaml";
        if (extension.endsWith(".nim")) return "nim";
        if (extension.endsWith(".pas")) return "pascal";
        if (extension.endsWith(".raku")) return "raku";
        if (extension.endsWith(".v")) return "v";
        if (extension.endsWith(".sh")) return "bash";
        if (extension.endsWith(".lua")) return "lua";
        if (extension.endsWith(".exs") || extension.endsWith(".ex")) return "elixir";
        if (extension.endsWith(".erl")) return "erlang";
        if (extension.endsWith(".pl")) return "perl";
        if (extension.endsWith(".hs")) return "haskell";
        if (extension.endsWith(".scala")) return "scala";
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