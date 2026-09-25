package com.aiinterview.backend.controller;

import com.aiinterview.backend.dto.roadmap.RoadmapResponse;
import com.aiinterview.backend.dto.roadmap.SwitchTrackRequest;
import com.aiinterview.backend.entity.User;
import com.aiinterview.backend.repository.UserRepository;
import com.aiinterview.backend.service.AIRoadmapService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/ai-roadmap")
@RequiredArgsConstructor
public class AIRoadmapController {

    private final AIRoadmapService aiRoadmapService;
    private final UserRepository userRepository;

    private User resolveUser(Authentication authentication) {
        if (authentication == null || authentication.getName() == null || authentication.getName().isBlank()) {
            return null;
        }
        String principalName = authentication.getName().trim();
        return userRepository.findByEmail(principalName)
                .or(() -> userRepository.findByUsername(principalName))
                .orElse(null);
    }

    @GetMapping
    public ResponseEntity<?> getRoadmap(Authentication authentication) {
        User user = resolveUser(authentication);
        if (user == null) {
            // Graceful safe fallback for guest or direct URL preview
            return ResponseEntity.ok(aiRoadmapService.getDefaultRoadmap());
        }

        RoadmapResponse response = aiRoadmapService.getUserRoadmap(user);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/suggestions")
    public ResponseEntity<?> getTrackSuggestions(@RequestParam(value = "q", required = false) String query) {
        return ResponseEntity.ok(aiRoadmapService.getTrackSuggestions(query));
    }

    @PostMapping("/track")
    public ResponseEntity<?> switchTrack(
            Authentication authentication,
            @Valid @RequestBody SwitchTrackRequest request
    ) {
        User user = resolveUser(authentication);
        if (user == null) {
            // Allow track switching preview for unauthenticated guests
            return ResponseEntity.ok(aiRoadmapService.getPreviewForTrack(request.getTrackId(), request.getCustomTitle()));
        }

        try {
            RoadmapResponse response = aiRoadmapService.switchTrack(user, request.getTrackId(), request.getCustomTitle());
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/milestone/{trackId}/{milestoneId}/toggle")
    public ResponseEntity<?> toggleMilestone(
            Authentication authentication,
            @PathVariable String trackId,
            @PathVariable String milestoneId
    ) {
        User user = resolveUser(authentication);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Please sign in to save and toggle your milestone progress."));
        }

        RoadmapResponse response = aiRoadmapService.toggleMilestone(user, trackId, milestoneId);
        return ResponseEntity.ok(response);
    }
}
