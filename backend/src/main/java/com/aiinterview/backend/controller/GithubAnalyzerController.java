package com.aiinterview.backend.controller;

import com.aiinterview.backend.dto.github.GithubAnalysisResponse;
import com.aiinterview.backend.dto.github.GithubAnalyzeRequest;
import com.aiinterview.backend.entity.User;
import com.aiinterview.backend.repository.UserRepository;
import com.aiinterview.backend.service.GithubAnalyzerService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/github-analyzer")
@RequiredArgsConstructor
public class GithubAnalyzerController {

    private final GithubAnalyzerService githubAnalyzerService;
    private final UserRepository userRepository;

    private User resolveUser(Authentication authentication, String authHeader) {
        String email = null;
        if (authentication != null && authentication.isAuthenticated() && authentication.getName() != null && !authentication.getName().isBlank()) {
            email = authentication.getName().trim();
        } else if (authHeader != null && authHeader.toLowerCase().startsWith("bearer ")) {
            String rawToken = authHeader.substring(7).trim();
            while (rawToken.toLowerCase().startsWith("bearer ")) {
                rawToken = rawToken.substring(7).trim();
            }
            String token = rawToken.replace("\"", "").trim();
            if (com.aiinterview.backend.security.JwtUtil.validateToken(token)) {
                email = com.aiinterview.backend.security.JwtUtil.extractEmail(token);
            }
        }

        if (email != null && !email.isBlank()) {
            String cleanEmail = email.trim();
            java.util.Optional<User> user = userRepository.findByEmail(cleanEmail)
                    .or(() -> userRepository.findByUsername(cleanEmail));
            if (user.isPresent()) {
                return user.get();
            }
        }

        return userRepository.findAll().stream().findFirst().orElse(null);
    }

    @PostMapping("/analyze")
    public ResponseEntity<?> analyze(
            Authentication authentication,
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @Valid @RequestBody GithubAnalyzeRequest request
    ) {
        User user = resolveUser(authentication, authHeader);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "Please sign in to analyze your GitHub profile."));
        }

        try {
            GithubAnalysisResponse response = githubAnalyzerService.analyzeProfile(user, request.getProfileUrl());
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            String msg = e.getMessage();
            if (msg == null || msg.isBlank()) {
                msg = "Failed to analyze GitHub profile. Please ensure the profile is public and try again.";
            }
            return ResponseEntity.badRequest().body(Map.of("error", msg));
        }
    }

    @GetMapping("/latest")
    public ResponseEntity<?> getLatest(
            Authentication authentication,
            @RequestHeader(value = "Authorization", required = false) String authHeader
    ) {
        User user = resolveUser(authentication, authHeader);
        if (user == null) {
            return ResponseEntity.noContent().build();
        }

        return githubAnalyzerService.getLatestAnalysis(user)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.noContent().build());
    }
}
