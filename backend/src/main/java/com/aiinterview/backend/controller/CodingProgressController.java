package com.aiinterview.backend.controller;

import com.aiinterview.backend.entity.CodingProblem;
import com.aiinterview.backend.entity.CodingProgress;
import com.aiinterview.backend.entity.User;
import com.aiinterview.backend.repository.CodingProblemRepository;
import com.aiinterview.backend.repository.UserRepository;
import com.aiinterview.backend.service.coding.CodingProgressService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/coding/progress")
public class CodingProgressController {

    private final CodingProgressService codingProgressService;
    private final UserRepository userRepository;
    private final CodingProblemRepository codingProblemRepository;

    public CodingProgressController(
            CodingProgressService codingProgressService,
            UserRepository userRepository,
            CodingProblemRepository codingProblemRepository
    ) {
        this.codingProgressService = codingProgressService;
        this.userRepository = userRepository;
        this.codingProblemRepository = codingProblemRepository;
    }

    @GetMapping
    public ResponseEntity<CodingProgress> getProgress(
            Authentication authentication
    ) {

        User user = getAuthenticatedUser(authentication);

        return ResponseEntity.ok(
                codingProgressService.getOrCreateProgress(user)
        );
    }

    @PutMapping("/problem/{problemId}")
    public ResponseEntity<CodingProgress> selectProblem(
            @PathVariable Long problemId,
            Authentication authentication
    ) {

        User user = getAuthenticatedUser(authentication);

        CodingProblem problem =
                getProblem(problemId);

        return ResponseEntity.ok(
                codingProgressService.saveCurrentProblem(
                        user,
                        problem
                )
        );
    }

    @PutMapping("/last-selected/{problemId}")
    public ResponseEntity<CodingProgress> saveLastSelectedProblem(
            @PathVariable Long problemId,
            Authentication authentication
    ) {

        User user = getAuthenticatedUser(authentication);

        CodingProblem problem =
                getProblem(problemId);

        return ResponseEntity.ok(
                codingProgressService.saveLastSelectedProblem(
                        user,
                        problem
                )
        );
    }

    @PutMapping("/code/{problemId}")
    public ResponseEntity<CodingProgress> saveCodeState(
            @PathVariable Long problemId,
            @RequestParam String language,
            @RequestBody String code,
            Authentication authentication
    ) {

        User user = getAuthenticatedUser(authentication);

        CodingProblem problem =
                getProblem(problemId);

        return ResponseEntity.ok(
                codingProgressService.saveCodeState(
                        user,
                        problem,
                        language,
                        code
                )
        );
    }

    @PutMapping("/complete/{problemId}")
    public ResponseEntity<CodingProgress> completeProblem(
            @PathVariable Long problemId,
            Authentication authentication
    ) {

        User user = getAuthenticatedUser(authentication);

        CodingProblem problem =
                getProblem(problemId);

        return ResponseEntity.ok(
                codingProgressService.markProblemCompleted(
                        user,
                        problem
                )
        );
    }

    @PutMapping("/submission")
    public ResponseEntity<CodingProgress> updateSubmission(
            @RequestParam boolean successful,
            Authentication authentication
    ) {

        User user = getAuthenticatedUser(authentication);

        return ResponseEntity.ok(
                codingProgressService.updateSubmission(
                        user,
                        successful
                )
        );
    }

    private CodingProblem getProblem(Long problemId) {

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

        if (authentication == null ||
                authentication.getName() == null ||
                authentication.getName().isBlank()) {

            throw new IllegalStateException(
                    "Authenticated user not found."
            );
        }

        return userRepository
                .findByEmail(authentication.getName())
                .orElseThrow(() ->
                        new IllegalStateException(
                                "User account not found."
                        )
                );
    }
}