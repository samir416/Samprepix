package com.aiinterview.backend.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "interview_answers")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InterviewAnswer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "session_id", nullable = false)
    private InterviewSession session;

    @Column(nullable = false)
    private Integer questionNumber;

    @Column(columnDefinition = "LONGTEXT", nullable = false)
    private String question;

    @Column(columnDefinition = "LONGTEXT", nullable = false)
    private String answer;

    @Column
    private Integer technicalAccuracy;

    @Column
    private Integer completeness;

    @Column
    private Integer communication;

    @Column
    private Integer overallScore;

    @Column(length = 50)
    private String performance;

    @Column(length = 30)
    private String difficulty;

    @Column(length = 100)
    private String nextFocusSkill;

    @Column(columnDefinition = "LONGTEXT")
    private String idealAnswer;

    @Column(columnDefinition = "LONGTEXT")
    private String feedback;

    @Column(columnDefinition = "LONGTEXT")
    private String strengths;

    @Column(columnDefinition = "LONGTEXT")
    private String missingConcepts;

    @Column(nullable = false, updatable = false)
    private LocalDateTime answeredAt;

        @PrePersist
    public void onCreate() {

        answeredAt = LocalDateTime.now();

        if (technicalAccuracy == null) {
            technicalAccuracy = 0;
        }

        if (completeness == null) {
            completeness = 0;
        }

        if (communication == null) {
            communication = 0;
        }

        if (overallScore == null) {
            overallScore = 0;
        }

        if (performance == null) {
            performance = "Not Evaluated";
        }

        if (difficulty == null) {
            difficulty = "Easy";
        }

        if (nextFocusSkill == null) {
            nextFocusSkill = "";
        }

        if (idealAnswer == null) {
            idealAnswer = "";
        }

        if (feedback == null) {
            feedback = "";
        }

        if (strengths == null) {
            strengths = "";
        }

        if (missingConcepts == null) {
            missingConcepts = "";
        }

    }

}
