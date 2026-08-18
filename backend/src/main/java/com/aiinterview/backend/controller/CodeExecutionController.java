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

    private final GitHubConnectionRepository
            gitHubConnectionRepository;

    private final CodingProgressService
            codingProgressService;

    private final FunctionExecutionWrapperService
            wrapperService;

    private final GitHubRepositoryService
            gitHubRepositoryService;

    public CodeExecutionController(
            CodeExecutionService codeExecutionService,
            CodingProblemRepository codingProblemRepository,
            UserRepository userRepository,
            GitHubConnectionRepository
                    gitHubConnectionRepository,
            CodingProgressService codingProgressService,
            FunctionExecutionWrapperService wrapperService,
            GitHubRepositoryService
                    gitHubRepositoryService
    ) {

        this.codeExecutionService =
                codeExecutionService;

        this.codingProblemRepository =
                codingProblemRepository;

        this.userRepository =
                userRepository;

        this.gitHubConnectionRepository =
                gitHubConnectionRepository;

        this.codingProgressService =
                codingProgressService;

        this.wrapperService =
                wrapperService;

        this.gitHubRepositoryService =
                gitHubRepositoryService;
    }

    @PostMapping("/execute")
    public ResponseEntity<CodeExecutionResponse> executeCode(
            @RequestBody CodeExecutionRequest request
    ) {

        CodeExecutionResponse response =
                codeExecutionService.execute(
                        request
                );

        return ResponseEntity.ok(
                response
        );
    }

    @PostMapping("/submit")
    public ResponseEntity<CodeExecutionResponse> submitCode(
            @RequestBody CodeExecutionRequest request,
            Authentication authentication
    ) {

        User user =
                getAuthenticatedUser(
                        authentication
                );

        CodeExecutionResponse response =
                codeExecutionService.execute(
                        request
                );

        boolean successful =
                response.isPassed();

        codingProgressService.updateSubmission(
                user,
                successful
        );

        if (!successful) {

            return ResponseEntity.ok(
                    response
            );
        }

        CodingProblem problem =
                codingProblemRepository
                        .findById(
                                request.getProblemId()
                        )
                        .orElseThrow(
                                () ->
                                        new IllegalStateException(
                                                "Coding problem not found."
                                        )
                        );

        codingProgressService
                .saveCodeState(
                        user,
                        problem,
                        request.getLanguage(),
                        request.getCode()
                );

        codingProgressService
                .markProblemCompleted(
                        user,
                        problem
                );

        GitHubConnection connection =
                gitHubConnectionRepository
                        .findByUser(user)
                        .orElse(null);

        if (connection == null ||
                connection.getAccessToken() == null ||
                connection.getAccessToken().isBlank() ||
                connection.getRepositoryUrl() == null ||
                connection.getRepositoryUrl().isBlank()) {

            response.setMessage(
                    "All test cases passed. Coding progress saved."
            );

            return ResponseEntity.ok(
                    response
            );
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

            String commitMessage =
                    "Solve: " +
                    problem.getTitle();

            GitHubRepositoryService.GitHubPushResult
                    pushResult =
                    gitHubRepositoryService.pushSolution(
                            user,
                            connection.getRepositoryUrl(),
                            solutionPath,
                            request.getCode(),
                            commitMessage
                    );

            response.setMessage(
                    "All test cases passed. Solution committed to GitHub."
            );

            return ResponseEntity.ok(
                    response
            );

        } catch (Exception exception) {

            response.setMessage(
                    "All test cases passed and progress was saved, but the GitHub commit failed: "
                            + safeMessage(
                                    exception.getMessage()
                            )
            );

            return ResponseEntity.ok(
                    response
            );
        }
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

        String safeFileName =
                fileName == null ||
                        fileName.isBlank()
                        ? "solution"
                        : fileName
                                .trim()
                                .replace(
                                        "\\",
                                        "/"
                                )
                                .replaceAll(
                                        "^/+",
                                        ""
                                );

        return "coding-solutions/" +
                problem.getId() +
                "-" +
                safeTitle +
                "/" +
                safeFileName;
    }

    private User getAuthenticatedUser(
            Authentication authentication
    ) {

        if (authentication == null ||
                authentication.getName() == null ||
                authentication.getName().isBlank()) {

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

        if (message == null ||
                message.isBlank()) {

            return "Unknown GitHub error.";
        }

        return message;
    }
}