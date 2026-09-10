package com.aiinterview.backend.dto.admin;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PlanRequest {
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
}
