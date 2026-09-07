package com.aiinterview.backend;

import com.aiinterview.backend.dto.performance.PerformanceAnalyticsDto;
import com.aiinterview.backend.entity.User;
import com.aiinterview.backend.repository.UserRepository;
import com.aiinterview.backend.service.performance.PerformanceService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
class PerformanceAnalyticsTest {

    @Autowired
    private PerformanceService performanceService;

    @Autowired
    private UserRepository userRepository;

    @Test
    @DisplayName("Performance Analytics returns non-null, user-scoped metrics without fake data")
    void testPerformanceAnalytics() {
        // Find existing test user or create a transient user
        User user = userRepository.findByEmail("samirprajapat5@gmail.com")
                .orElseGet(() -> userRepository.findAll().stream().findFirst().orElse(null));

        assertNotNull(user, "At least one user must exist in the database for performance testing");

        PerformanceAnalyticsDto analytics = performanceService.getPerformanceAnalytics(user);
        assertNotNull(analytics, "Performance analytics DTO must not be null");

        // Verify coding metrics
        assertTrue(analytics.getProblemsSolved() >= 0, "Problems solved must be non-negative");
        assertTrue(analytics.getProblemsAttempted() >= analytics.getProblemsSolved(),
                "Problems attempted must be >= problems solved");
        assertTrue(analytics.getAcceptanceRate() >= 0.0 && analytics.getAcceptanceRate() <= 100.0,
                "Acceptance rate must be between 0 and 100%");
        assertTrue(analytics.getCurrentStreak() >= 0, "Streak must be non-negative");

        // Verify Skill Metrics (Real categories)
        assertNotNull(analytics.getSkillMetrics(), "Skill metrics list must not be null");
        assertFalse(analytics.getSkillMetrics().isEmpty(), "Skill metrics must contain categories");
        boolean hasDsa = analytics.getSkillMetrics().stream().anyMatch(s -> s.getName().contains("DSA"));
        boolean hasDb = analytics.getSkillMetrics().stream().anyMatch(s -> s.getName().contains("Database"));
        assertTrue(hasDsa, "Must track DSA skill progress");
        assertTrue(hasDb, "Must track Database skill progress");

        // Verify Composite Placement Readiness
        assertTrue(analytics.getPlacementReadinessScore() >= 0 && analytics.getPlacementReadinessScore() <= 100,
                "Readiness score must be between 0 and 100");
        assertNotNull(analytics.getReadinessStatus(), "Readiness status must not be null");

        // Verify Problem Solving score derived from authentic data
        if (analytics.getProblemsAttempted() > 0) {
            assertNotNull(analytics.getProblemSolvingScore(), "Problem solving score should be non-null when problems attempted");
            assertTrue(analytics.getProblemSolvingScore() >= 0.0 && analytics.getProblemSolvingScore() <= 100.0);
        }

        // Verify no fake interview data when user has no interviews
        if (!analytics.isHasInterviewData()) {
            assertNull(analytics.getAvgInterviewScore(),
                    "Average interview score must be null when user has no completed interviews");
            assertEquals(0, analytics.getTotalInterviews());
        } else {
            assertNotNull(analytics.getAvgInterviewScore());
            assertTrue(analytics.getAvgInterviewScore() > 0);
        }

        // Verify timeline integrity
        assertNotNull(analytics.getTimeline(), "Timeline must not be null");
        System.out.printf("[PERFORMANCE TEST PASSED] Solved: %d, Attempted: %d, Acceptance: %.1f%%, Readiness: %d%% (%s)%n",
                analytics.getProblemsSolved(),
                analytics.getProblemsAttempted(),
                analytics.getAcceptanceRate(),
                analytics.getPlacementReadinessScore(),
                analytics.getReadinessStatus());
    }
}
