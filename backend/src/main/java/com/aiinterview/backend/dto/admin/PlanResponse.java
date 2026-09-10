package com.aiinterview.backend.dto.admin;

import com.aiinterview.backend.entity.Plan;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PlanResponse {
    private Long id;
    private String name;
    private String description;
    private double priceInr;
    private double priceUsd;
    private String interval;
    private int maxMockInterviews;
    private int maxResumeScans;
    private int maxCodingProblems;
    private int maxAptitudeQuestions;
    private boolean includesAIHints;
    private boolean includesAnalytics;
    private boolean includesTier1Companies;
    private boolean includesPriorityCompute;
    private boolean active;
    private boolean featured;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static PlanResponse fromEntity(Plan plan) {
        return PlanResponse.builder()
                .id(plan.getId())
                .name(plan.getName())
                .description(plan.getDescription())
                .priceInr(plan.getPriceInr())
                .priceUsd(plan.getPriceUsd())
                .interval(plan.getInterval())
                .maxMockInterviews(plan.getMaxMockInterviews())
                .maxResumeScans(plan.getMaxResumeScans())
                .maxCodingProblems(plan.getMaxCodingProblems())
                .maxAptitudeQuestions(plan.getMaxAptitudeQuestions())
                .includesAIHints(plan.isIncludesAIHints())
                .includesAnalytics(plan.isIncludesAnalytics())
                .includesTier1Companies(plan.isIncludesTier1Companies())
                .includesPriorityCompute(plan.isIncludesPriorityCompute())
                .active(plan.isActive())
                .featured(plan.isFeatured())
                .createdAt(plan.getCreatedAt())
                .updatedAt(plan.getUpdatedAt())
                .build();
    }
}
