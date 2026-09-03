package com.aiinterview.backend.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "coding_problem_completions",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_user_problem_completion",
                        columnNames = {"user_id", "problem_id"}
                )
        }
)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CodingProblemCompletion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "user_id",
            nullable = false
    )
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "problem_id",
            nullable = false
    )
    private CodingProblem problem;

    @Column(nullable = false)
    @Builder.Default
    private boolean completed = false;

    @Column(nullable = false)
    @Builder.Default
    private Integer submissionCount = 0;

    @Column(nullable = false)
    @Builder.Default
    private Integer successfulSubmissionCount = 0;

    @Column(length = 30)
    private String language;

    @Lob
    @Column(columnDefinition = "LONGTEXT")
    private String submittedCode;

    private LocalDateTime firstAttemptAt;

    private LocalDateTime completedAt;

    private LocalDateTime lastAttemptAt;

    @PrePersist
    public void onCreate() {

        if (submissionCount == null) {
            submissionCount = 0;
        }

        if (successfulSubmissionCount == null) {
            successfulSubmissionCount = 0;
        }

        lastAttemptAt = LocalDateTime.now();
    }

    @PreUpdate
    public void onUpdate() {

        lastAttemptAt = LocalDateTime.now();
    }
}