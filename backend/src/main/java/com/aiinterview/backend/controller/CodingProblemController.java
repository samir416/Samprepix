package com.aiinterview.backend.controller;

import com.aiinterview.backend.entity.CodingProblem;
import com.aiinterview.backend.entity.User;
import com.aiinterview.backend.repository.UserRepository;
import com.aiinterview.backend.service.coding.CodingProblemService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/coding/problems")
public class CodingProblemController {

    private final CodingProblemService codingProblemService;
    private final UserRepository userRepository;

    public CodingProblemController(
            CodingProblemService codingProblemService,
            UserRepository userRepository
    ) {
        this.codingProblemService = codingProblemService;
        this.userRepository = userRepository;
    }

    @GetMapping
    public ResponseEntity<List<CodingProblem>> getProblems(
            Authentication authentication
    ) {

        User user = getAuthenticatedUser(authentication);

        Integer experienceLevel =
                getExperienceLevel(user);

        return ResponseEntity.ok(
                codingProblemService
                        .getProblemsForExperience(
                                experienceLevel
                        )
        );
    }

    @GetMapping("/{problemId}")
    public ResponseEntity<CodingProblem> getProblem(
            @PathVariable Long problemId
    ) {

        return ResponseEntity.ok(
                codingProblemService
                        .getProblemById(problemId)
        );
    }

    @GetMapping("/difficulty/{difficulty}")
    public ResponseEntity<List<CodingProblem>> getProblemsByDifficulty(
            @PathVariable String difficulty,
            Authentication authentication
    ) {

        User user = getAuthenticatedUser(authentication);

        Integer experienceLevel =
                getExperienceLevel(user);

        List<CodingProblem> problems =
                codingProblemService
                        .getProblemsForExperience(
                                experienceLevel
                        );

        List<CodingProblem> filteredProblems =
                problems.stream()
                        .filter(problem ->
                                problem.getDifficulty()
                                        .equalsIgnoreCase(
                                                difficulty
                                        )
                        )
                        .toList();

        return ResponseEntity.ok(
                filteredProblems
        );
    }

    private Integer getExperienceLevel(
            User user
    ) {

        if (user.getProfile() == null ||
                user.getProfile()
                        .getExperienceLevel() == null ||
                user.getProfile()
                        .getExperienceLevel()
                        .isBlank()) {

            return 1;
        }

        String experience =
                user.getProfile()
                        .getExperienceLevel()
                        .trim()
                        .toUpperCase();

        return switch (experience) {

            case "BEGINNER" -> 1;

            case "INTERMEDIATE" -> 2;

            case "ADVANCED" -> 3;

            default -> 1;
        };
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