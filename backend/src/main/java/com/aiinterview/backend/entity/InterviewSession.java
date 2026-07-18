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
    private String interviewType;

    @Column(nullable = false)
    private Integer totalQuestions;

    @Column(nullable = false)
    private Integer currentQuestion;

    @Column(columnDefinition = "TEXT")
    private String currentQuestionText;

    @Column(nullable = false)
    private Integer score;

    @Column
    private Double percentage;

    @Column(columnDefinition = "TEXT")
    private String overallFeedback;

    @Column(columnDefinition = "TEXT")
    private String strengths;

    @Column(columnDefinition = "TEXT")
    private String weaknesses;

    @Column(columnDefinition = "TEXT")
    private String suggestions;

    @Column(nullable = false)
    private String status;

    @Column(nullable = false, updatable = false)
    private LocalDateTime startedAt;

    private LocalDateTime completedAt;

    @PrePersist
    public void onCreate() {
        this.startedAt = LocalDateTime.now();

        if (this.currentQuestion == null) {
            this.currentQuestion = 0;
        }

        if (this.score == null) {
            this.score = 0;
        }

        if (this.status == null) {
            this.status = "IN_PROGRESS";
        }
    }
}