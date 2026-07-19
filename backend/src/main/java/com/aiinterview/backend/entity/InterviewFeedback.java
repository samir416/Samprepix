package com.aiinterview.backend.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "interview_feedback")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InterviewFeedback {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * User who submitted the feedback
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    /**
     * Interview Session
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "session_id", nullable = false)
    private InterviewSession interviewSession;

    /**
     * Rating (1-5)
     */
    @Column(nullable = false)
    private Integer rating;

    /**
     * Optional Suggestion
     */
    @Column(length = 1000)
    private String suggestion;

    /**
     * Feedback Moderation Status
     * Default = PENDING
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private FeedbackStatus status = FeedbackStatus.PENDING;

    /**
     * Feedback Submitted Time
     */
    @Column(nullable = false)
    private LocalDateTime createdAt;

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();

        // Safety check in case Builder.Default is bypassed
        if (this.status == null) {
            this.status = FeedbackStatus.PENDING;
        }
    }
}