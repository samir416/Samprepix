package com.aiinterview.backend.dto.admin;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PlanRequest {

    @NotBlank(message = "Plan name is required")
    @Size(max = 30, message = "Plan name must not exceed 30 characters")
    private String name;

    @NotBlank(message = "Plan description is required")
    @Size(max = 50, message = "Plan description must not exceed 50 characters")
    private String description;

    @DecimalMin(value = "0.0", message = "INR price cannot be negative")
    @DecimalMax(value = "1000000.0", message = "INR price is too high")
    private double priceInr;

    @DecimalMin(value = "0.0", message = "USD price cannot be negative")
    @DecimalMax(value = "100000.0", message = "USD price is too high")
    private double priceUsd;

    @NotBlank(message = "Plan interval is required")
    @Size(max = 20, message = "Plan interval is invalid")
    private String interval;

    @Min(value = 0, message = "Mock interview limit cannot be negative")
    @Max(value = 1000000, message = "Mock interview limit is too high")
    private int maxMockInterviews;

    @Min(value = 0, message = "Resume scan limit cannot be negative")
    @Max(value = 1000000, message = "Resume scan limit is too high")
    private int maxResumeScans;

    @Min(value = 0, message = "Coding problem limit cannot be negative")
    @Max(value = 1000000, message = "Coding problem limit is too high")
    private int maxCodingProblems;

    @Min(value = 0, message = "Aptitude question limit cannot be negative")
    @Max(value = 1000000, message = "Aptitude question limit is too high")
    private int maxAptitudeQuestions;

    private boolean includesAIHints;

    private boolean includesAnalytics;

    private boolean includesTier1Companies;

    private boolean includesPriorityCompute;

    private boolean active;

    private boolean featured;
}
