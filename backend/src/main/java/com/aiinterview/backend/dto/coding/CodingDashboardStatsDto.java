package com.aiinterview.backend.dto.coding;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CodingDashboardStatsDto {

    private int problemsSolved;
    private int problemsAttempted;
    private int totalSubmissions;
    private int successfulSubmissions;
    private double acceptanceRate;

    private int easySolved;
    private int mediumSolved;
    private int hardSolved;

    private int dsaSolved;
    private int sqlSolved;

    private long totalAvailableProblems;
    private int currentStreak;

    @Builder.Default
    private List<TimelinePointDto> timeline = new ArrayList<>();

    @Builder.Default
    private List<LanguageCountDto> languageDistribution = new ArrayList<>();

    @Builder.Default
    private List<RecentSubmissionDto> recentSubmissions = new ArrayList<>();

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TimelinePointDto {
        private String date;
        private int solved;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class LanguageCountDto {
        private String language;
        private int count;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RecentSubmissionDto {
        private Long problemId;
        private String problemTitle;
        private String difficulty;
        private String category;
        private String language;
        private boolean completed;
        private LocalDateTime attemptedAt;
    }
}
