package com.aiinterview.backend.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "plans")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Plan {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 30)
    private String name;

    @Column(nullable = false, length = 50)
    private String description;

    @Column(nullable = false)
    private double priceInr;

    @Column(nullable = false)
    private double priceUsd;

    @Column(name = "plan_interval", nullable = false, length = 20)
    private String interval;

    @Column(nullable = false)
    private int maxMockInterviews;

    @Column(nullable = false)
    private int maxResumeScans;

    @Column(nullable = false)
    private int maxCodingProblems;

    @Column(nullable = false)
    private int maxAptitudeQuestions;

    @Column(nullable = false)
    private boolean includesAIHints;

    @Column(nullable = false)
    private boolean includesAnalytics;

    @Column(nullable = false)
    private boolean includesTier1Companies;

    @Column(nullable = false)
    private boolean includesPriorityCompute;

    @Column(nullable = false)
    private boolean active;

    @Column(nullable = false)
    private boolean featured;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    public void onCreate() {
        LocalDateTime now = LocalDateTime.now();

        if (createdAt == null) {
            createdAt = now;
        }

        if (updatedAt == null) {
            updatedAt = now;
        }

        if (interval == null || interval.isBlank()) {
            interval = "MONTH";
        }
    }

    @PreUpdate
    public void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}