package com.aiinterview.backend.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "aptitude_attempts",
        indexes = {
                @Index(name = "idx_aptitude_attempt_user", columnList = "user_id"),
                @Index(name = "idx_aptitude_attempt_track", columnList = "track_id"),
                @Index(name = "idx_aptitude_attempt_date", columnList = "completed_at")
        }
)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AptitudeAttempt {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "track_id", nullable = false, length = 64)
    private String trackId;

    @Column(name = "track_title", nullable = false, length = 128)
    private String trackTitle;

    @Column(name = "total_questions", nullable = false)
    private int totalQuestions;

    @Column(name = "correct_count", nullable = false)
    private int correctCount;

    @Column(name = "incorrect_count", nullable = false)
    private int incorrectCount;

    @Column(name = "unanswered_count", nullable = false)
    private int unansweredCount;

    @Column(nullable = false)
    private int score;

    @Column(nullable = false)
    private double percentage;

    @Column(nullable = false)
    private double accuracy;

    @Column(name = "time_spent_seconds", nullable = false)
    private int timeSpentSeconds;

    @Column(name = "time_limit_seconds", nullable = false)
    private int timeLimitSeconds;

    @Column(name = "completion_reason", nullable = false, length = 32)
    private String completionReason; // "USER_SUBMITTED" or "TIMER_EXPIRED"

    @Column(name = "completed_at", nullable = false)
    private LocalDateTime completedAt;

    @Lob
    @Column(name = "answers_json", columnDefinition = "LONGTEXT")
    private String answersJson;

    @Lob
    @Column(name = "category_breakdown_json", columnDefinition = "TEXT")
    private String categoryBreakdownJson;

    @Lob
    @Column(name = "difficulty_breakdown_json", columnDefinition = "TEXT")
    private String difficultyBreakdownJson;

    @PrePersist
    public void prePersist() {
        if (completedAt == null) {
            completedAt = LocalDateTime.now();
        }
    }
}
