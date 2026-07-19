package com.aiinterview.backend.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "feedback_approval_tokens")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FeedbackApprovalToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Secure unique token used in email approval links.
     */
    @Column(nullable = false, unique = true, length = 64)
    private String token;

    /**
     * Associated interview feedback.
     */
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "feedback_id", nullable = false, unique = true)
    private InterviewFeedback feedback;

    /**
     * Prevents token reuse.
     */
    @Builder.Default
    @Column(nullable = false)
    private boolean used = false;

    /**
     * Token creation timestamp.
     */
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}