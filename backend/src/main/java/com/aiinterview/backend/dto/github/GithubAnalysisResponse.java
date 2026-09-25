package com.aiinterview.backend.dto.github;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GithubAnalysisResponse {

    private String username;
    private String name;
    private String avatarUrl;
    private String profileUrl;
    private String bio;
    private String location;
    private String company;
    private String blog;
    private int publicRepos;
    private int followers;
    private int following;

    private int overallScore;
    private List<CategoryScoreDto> categoryScores;
    private List<String> deductions;
    private List<String> improvements;
    private List<Map<String, Object>> topLanguages;

    private String currentReadme;
    private String recommendedReadme;
    private Map<String, List<String>> readmeSuggestions; // keep, improve, remove, add

    private List<RepoAnalysisDto> repoAnalyses;
    private RecruiterViewDto recruiterView;

    private boolean premium;
    private String effectivePlan;
    private String analyzedAt;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class CategoryScoreDto {
        private String category;
        private int score;
        private int maxScore;
        private String feedback;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class RepoAnalysisDto {
        private String name;
        private String htmlUrl;
        private String description;
        private String language;
        private int stars;
        private int forks;
        private List<String> recommendations;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class RecruiterViewDto {
        private String immediateImpressions;
        private List<String> demonstratedSkills;
        private List<String> missingSignals;
        private String evaluationVerdict;
        private String actionableAdvice;
    }
}
