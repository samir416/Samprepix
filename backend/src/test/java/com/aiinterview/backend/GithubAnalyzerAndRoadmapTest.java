package com.aiinterview.backend;

import com.aiinterview.backend.dto.roadmap.RoadmapResponse;
import com.aiinterview.backend.entity.User;
import com.aiinterview.backend.repository.UserRepository;
import com.aiinterview.backend.service.AIRoadmapService;
import com.aiinterview.backend.service.GithubAnalyzerService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
class GithubAnalyzerAndRoadmapTest {

    @Autowired
    private AIRoadmapService aiRoadmapService;

    @Autowired
    private GithubAnalyzerService githubAnalyzerService;

    @Autowired
    private UserRepository userRepository;

    @Test
    @DisplayName("AIRoadmapService generates curated tracks, phases, milestones and tracks XP")
    void testAIRoadmapService() {
        User user = userRepository.findAll().stream().findFirst().orElse(null);
        assertNotNull(user, "User must exist for test");

        RoadmapResponse roadmap = aiRoadmapService.getUserRoadmap(user);
        assertNotNull(roadmap, "Roadmap must not be null");
        assertNotNull(roadmap.getTrackId(), "Track ID must be present");
        assertNotNull(roadmap.getTrackTitle(), "Track title must be present");
        assertNotNull(roadmap.getPhases(), "Phases must be present");
        assertFalse(roadmap.getPhases().isEmpty(), "Phases list must not be empty");
        assertTrue(roadmap.getPhases().size() >= 7, "Roadmap should have at least 7 progression phases");

        // Verify milestone structure
        RoadmapResponse.RoadmapPhaseDto firstPhase = roadmap.getPhases().get(0);
        assertNotNull(firstPhase.getMilestones());
        assertFalse(firstPhase.getMilestones().isEmpty());

        String milestoneId = firstPhase.getMilestones().get(0).getId();

        // Toggle milestone completion
        RoadmapResponse updatedRoadmap = aiRoadmapService.toggleMilestone(user, roadmap.getTrackId(), milestoneId);
        assertNotNull(updatedRoadmap);
        assertTrue(updatedRoadmap.getEarnedXp() > 0, "Earned XP should increase when milestone is completed");

        // Untoggle milestone completion
        RoadmapResponse untoggledRoadmap = aiRoadmapService.toggleMilestone(user, roadmap.getTrackId(), milestoneId);
        assertNotNull(untoggledRoadmap);

        // Switch track
        RoadmapResponse switched = aiRoadmapService.switchTrack(user, "backend");
        assertEquals("backend", switched.getTrackId());
    }

    @Test
    @DisplayName("GithubAnalyzerService enforces SSRF defense and URL validation")
    void testGithubAnalyzerSSRF() {
        // Valid username extraction
        String username = githubAnalyzerService.validateAndExtractUsername("https://github.com/torvalds");
        assertEquals("torvalds", username);

        String usernameWithSlash = githubAnalyzerService.validateAndExtractUsername("https://github.com/torvalds/");
        assertEquals("torvalds", usernameWithSlash);

        // Reject SSRF attempts
        assertThrows(IllegalArgumentException.class, () ->
                githubAnalyzerService.validateAndExtractUsername("http://localhost:8080/admin"));

        assertThrows(IllegalArgumentException.class, () ->
                githubAnalyzerService.validateAndExtractUsername("http://127.0.0.1:2000"));

        assertThrows(IllegalArgumentException.class, () ->
                githubAnalyzerService.validateAndExtractUsername("https://evil-site.com/user"));

        assertThrows(IllegalArgumentException.class, () ->
                githubAnalyzerService.validateAndExtractUsername("file:///etc/passwd"));

        assertThrows(IllegalArgumentException.class, () ->
                githubAnalyzerService.validateAndExtractUsername("javascript:alert(1)"));

        assertThrows(IllegalArgumentException.class, () ->
                githubAnalyzerService.validateAndExtractUsername(""));

        assertThrows(IllegalArgumentException.class, () ->
                githubAnalyzerService.validateAndExtractUsername("https://github.com/settings"));
    }

    @Test
    @DisplayName("analyzeProfile successfully analyzes public profile https://github.com/samir416")
    void testAnalyzeProfileSamir416() {
        User user = userRepository.findAll().stream().findFirst().orElse(null);
        assertNotNull(user, "User must exist for test");

        try {
            var response = githubAnalyzerService.analyzeProfile(user, "https://github.com/samir416");
            assertNotNull(response);
            assertEquals("samir416", response.getUsername());
            assertTrue(response.getOverallScore() > 0);
            assertNotNull(response.getCategoryScores());
            assertFalse(response.getCategoryScores().isEmpty());
        } catch (IllegalStateException e) {
            assertTrue(e.getMessage().contains("rate limit"));
        }
    }

    @Test
    @DisplayName("analyzeProfile produces deterministic overallScore and categoryScores for identical profile")
    void testDeterministicScoreAcrossInvocations() {
        User user = userRepository.findAll().stream().findFirst().orElse(null);
        assertNotNull(user, "User must exist for test");

        try {
            var res1 = githubAnalyzerService.analyzeProfile(user, "https://github.com/samir416");
            var res2 = githubAnalyzerService.analyzeProfile(user, "https://github.com/samir416");

            assertEquals(res1.getOverallScore(), res2.getOverallScore(), "Overall score must be strictly deterministic");
            assertEquals(res1.getCategoryScores().size(), res2.getCategoryScores().size());
            for (int i = 0; i < res1.getCategoryScores().size(); i++) {
                assertEquals(res1.getCategoryScores().get(i).getScore(), res2.getCategoryScores().get(i).getScore());
            }
        } catch (IllegalStateException e) {
            assertTrue(e.getMessage().contains("rate limit"));
        }
    }
}
