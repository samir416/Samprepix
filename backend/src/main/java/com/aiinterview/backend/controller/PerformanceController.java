package com.aiinterview.backend.controller;

import com.aiinterview.backend.dto.performance.PerformanceAnalyticsDto;
import com.aiinterview.backend.entity.User;
import com.aiinterview.backend.repository.UserRepository;
import com.aiinterview.backend.service.performance.PerformanceService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/performance")
public class PerformanceController {

    private final PerformanceService performanceService;
    private final UserRepository userRepository;

    public PerformanceController(PerformanceService performanceService, UserRepository userRepository) {
        this.performanceService = performanceService;
        this.userRepository = userRepository;
    }

    @GetMapping("/analytics")
    public ResponseEntity<PerformanceAnalyticsDto> getPerformanceAnalytics(Authentication authentication) {
        if (authentication == null || authentication.getName() == null) {
            return ResponseEntity.status(401).build();
        }

        User user = userRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new RuntimeException("User not found: " + authentication.getName()));

        return ResponseEntity.ok(performanceService.getPerformanceAnalytics(user));
    }
}
