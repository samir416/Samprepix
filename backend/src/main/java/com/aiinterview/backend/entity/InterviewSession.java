package com.aiinterview.backend.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "interview_sessions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InterviewSession {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false)
    private String targetRole;

    @Column(nullable = false)
private String interviewType;

    @Column(nullable = false)
    private String experienceLevel;

    @Column(columnDefinition = "LONGTEXT")
    private String selectedSkills;

    @Column(columnDefinition = "LONGTEXT")
    private String weakAreas;

    @Column(columnDefinition = "LONGTEXT")
    private String strongAreas;

    @Column(columnDefinition = "TEXT")
    private String currentQuestion;

    @Column(columnDefinition = "LONGTEXT")
    private String previousQuestions;

    @Column(columnDefinition = "LONGTEXT")
    private String previousAnswers;

    @Column
    private Integer questionsAnswered;

    @Column
    private Integer overallScore;

    @Column
    private Integer technicalAccuracy;

    @Column
    private Integer completeness;

    @Column
    private Integer communication;

    @Column
    private Boolean interviewEndedByUser;

    @Column
    private Boolean reportGenerated;

    @Column
    private String nextFocusSkill;

    @Column
    private String difficulty;

    @Column(nullable = false)
    private String status;

    @Column(nullable = false, updatable = false)
    private LocalDateTime startedAt;

    private LocalDateTime completedAt;

        @PrePersist
    public void onCreate() {

        startedAt = LocalDateTime.now();

        if (questionsAnswered == null) {
            questionsAnswered = 0;
        }

        if (overallScore == null) {
            overallScore = 0;
        }

        if (technicalAccuracy == null) {
            technicalAccuracy = 0;
        }

        if (completeness == null) {
            completeness = 0;
        }

        if (communication == null) {
            communication = 0;
        }

        if (interviewEndedByUser == null) {
            interviewEndedByUser = false;
        }

        if (reportGenerated == null) {
            reportGenerated = false;
        }

        if (previousQuestions == null) {
            previousQuestions = "";
        }

        if (previousAnswers == null) {
            previousAnswers = "";
        }

        if (selectedSkills == null) {
            selectedSkills = "";
        }

        if (weakAreas == null) {
            weakAreas = "";
        }

        if (strongAreas == null) {
            strongAreas = "";
        }

        if (difficulty == null) {
            difficulty = "Easy";
        }

        if (status == null) {
            status = "IN_PROGRESS";
        }

    }

}
