package com.aiinterview.backend.controller;

import com.aiinterview.backend.entity.CodingProblem;
import com.aiinterview.backend.entity.CodingProblemCompletion;
import com.aiinterview.backend.entity.User;
import com.aiinterview.backend.repository.CodingProblemRepository;
import com.aiinterview.backend.repository.UserRepository;
import com.aiinterview.backend.service.coding.CodingProblemCompletionService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/coding/completions")
public class CodingProblemCompletionController {

    private final CodingProblemCompletionService completionService;
    private final UserRepository userRepository;
    private final CodingProblemRepository codingProblemRepository;

    public CodingProblemCompletionController(
            CodingProblemCompletionService completionService,
            UserRepository userRepository,
            CodingProblemRepository codingProblemRepository
    ) {
        this.completionService = completionService;
        this.userRepository = userRepository;
        this.codingProblemRepository = codingProblemRepository;
    }

    @GetMapping
    public ResponseEntity<List<CodingProblemCompletion>> getUserCompletions(
            Authentication authentication
    ) {

        User user =
                getAuthenticatedUser(authentication);

        return ResponseEntity.ok(
                completionService.getUserCompletions(user)
        );
    }

    @GetMapping("/completed")
    public ResponseEntity<List<CodingProblemCompletion>> getCompletedProblems(
            Authentication authentication
    ) {

        User user =
                getAuthenticatedUser(authentication);

        return ResponseEntity.ok(
                completionService.getCompletedProblems(user)
        );
    }

    @GetMapping("/count")
    public ResponseEntity<Long> getCompletedProblemCount(
            Authentication authentication
    ) {

        User user =
                getAuthenticatedUser(authentication);

        return ResponseEntity.ok(
                completionService.getCompletedProblemCount(user)
        );
    }

    @GetMapping("/{problemId}")
    public ResponseEntity<CodingProblemCompletion> getCompletion(
            @PathVariable Long problemId,
            Authentication authentication
    ) {

        User user =
                getAuthenticatedUser(authentication);

        CodingProblem problem =
                getProblem(problemId);

        CodingProblemCompletion completion =
                completionService.getCompletion(
                        user,
                        problem
                );

        return ResponseEntity.ok(
                completion
        );
    }

    @GetMapping("/{problemId}/completed")
    public ResponseEntity<Boolean> isProblemCompleted(
            @PathVariable Long problemId,
            Authentication authentication
    ) {

        User user =
                getAuthenticatedUser(authentication);

        CodingProblem problem =
                getProblem(problemId);

        return ResponseEntity.ok(
                completionService.isProblemCompleted(
                        user,
                        problem
                )
        );
    }

    private CodingProblem getProblem(
            Long problemId
    ) {

        if (problemId == null) {

            throw new IllegalArgumentException(
                    "Problem ID cannot be null."
            );
        }

        return codingProblemRepository
                .findById(problemId)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Coding problem not found."
                        )
                );
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
                .orElseThrow(() ->
                        new IllegalStateException(
                                "User account not found."
                        )
                );
    }
}