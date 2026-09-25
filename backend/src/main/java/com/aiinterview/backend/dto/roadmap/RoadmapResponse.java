package com.aiinterview.backend.dto.roadmap;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RoadmapResponse {

    private String trackId;
    private String trackTitle;
    private String detectedTrack;
    private String description;
    private int totalMilestones;
    private int completedMilestonesCount;
    private int progressPercentage;
    private int totalXp;
    private int earnedXp;
    private String currentLevel;
    private List<RoadmapPhaseDto> phases;
    private List<TrackOptionDto> availableTracks;
    private boolean premium;
    private String effectivePlan;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class RoadmapPhaseDto {
        private String phaseId;
        private int phaseNumber;
        private String title;
        private String subtitle;
        private boolean locked;
        private List<MilestoneDto> milestones;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class MilestoneDto {
        private String id;
        private String title;
        private String description;
        private String estimatedTime;
        private int xp;
        private List<String> skills;
        private String recommendedProject;
        private boolean completed;
        private boolean locked;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class TrackOptionDto {
        private String id;
        private String title;
        private String description;
        private String iconName;
    }
}
