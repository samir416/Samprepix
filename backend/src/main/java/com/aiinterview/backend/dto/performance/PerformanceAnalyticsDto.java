package com.aiinterview.backend.dto.performance;

import lombok.*;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PerformanceAnalyticsDto {

    // Composite Placement Readiness (0-100)
    private int placementReadinessScore;
    private String readinessStatus;
    private Double technicalScore;
    private Double problemSolvingScore;
    private Integer resumeAtsScore;

    // Real Coding Metrics
    private int problemsSolved;
    private int problemsAttempted;
    private int totalSubmissions;
    private double acceptanceRate;
    private int easySolved;
    private int mediumSolved;
    private int hardSolved;
    private int dsaSolved;
    private int sqlSolved;
    private int currentStreak;

    // Skill Progress Breakdown (Real category data)
    private List<SkillMetricDto> skillMetrics;

    // Real Coding Progress Timeline for Chart
    private List<TimelinePointDto> timeline;

    // Real Mock Interview Metrics
    private boolean hasInterviewData;
    private int totalInterviews;
    private Double avgInterviewScore;
    private Double avgTechnicalAccuracy;
    private Double avgCommunication;
    private Double avgCompleteness;
    private List<RecentInterviewDto> recentInterviews;

    // Real Resume ATS Metrics
    private boolean hasResumeData;
    private Integer latestResumeScore;
    private String resumeAnalyzedAt;

    // Real Combined Recent Activity Log
    private List<ActivityLogDto> recentActivities;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SkillMetricDto {
        private String name;
        private int solved;
        private int total;
        private double progress;
        private String color;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TimelinePointDto {
        private String date;
        private int cumulativeSolved;
        private int dailySolved;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RecentInterviewDto {
        private Long id;
        private String targetRole;
        private String interviewType;
        private Integer overallScore;
        private Integer technicalAccuracy;
        private Integer communication;
        private LocalDateTime completedAt;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ActivityLogDto {
        private String type; // "CODING", "INTERVIEW", "RESUME"
        private String title;
        private String status;
        private String score;
        private String timestamp;
    }
}
