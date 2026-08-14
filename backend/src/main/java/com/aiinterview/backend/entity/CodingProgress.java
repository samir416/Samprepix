package com.aiinterview.backend.entity;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(
        name = "coding_progress",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_coding_progress_user",
                        columnNames = "user_id"
                )
        }
)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CodingProgress {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "user_id",
            nullable = false,
            unique = true
    )
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "current_problem_id")
    private CodingProblem currentProblem;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "last_selected_problem_id")
    private CodingProblem lastSelectedProblem;

    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(
            name = "coding_completed_problems",
            joinColumns = @JoinColumn(name = "progress_id")
    )
    @Column(name = "problem_id", nullable = false)
    @Builder.Default
    private Set<Long> completedProblemIds = new HashSet<>();

    @Column(length = 30)
    private String lastLanguage;

    @Column(columnDefinition = "LONGTEXT")
    private String lastCode;

    @Column(nullable = false)
    @Builder.Default
    private Integer completedProblems = 0;

    @Column(nullable = false)
    @Builder.Default
    private Integer totalSubmissions = 0;

    @Column(nullable = false)
    @Builder.Default
    private Integer successfulSubmissions = 0;

    @Column(nullable = false)
    @Builder.Default
    private boolean started = false;

    @Column(nullable = false)
    @Builder.Default
    private boolean completed = false;

    private LocalDateTime startedAt;

    private LocalDateTime lastActivityAt;

    private LocalDateTime completedAt;

    @PrePersist
    public void onCreate() {

        LocalDateTime now = LocalDateTime.now();

        if (completedProblemIds == null) {
            completedProblemIds = new HashSet<>();
        }

        if (completedProblems == null) {
            completedProblems = completedProblemIds.size();
        }

        if (totalSubmissions == null) {
            totalSubmissions = 0;
        }

        if (successfulSubmissions == null) {
            successfulSubmissions = 0;
        }

        lastActivityAt = now;
    }

    @PreUpdate
    public void onUpdate() {

        if (completedProblemIds == null) {
            completedProblemIds = new HashSet<>();
        }

        completedProblems = completedProblemIds.size();
        lastActivityAt = LocalDateTime.now();
    }
}