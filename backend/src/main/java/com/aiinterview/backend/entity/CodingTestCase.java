package com.aiinterview.backend.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(
        name = "coding_test_cases",
        indexes = {
                @Index(name = "idx_test_cases_problem_active_hidden", columnList = "problem_id, active, hidden"),
                @Index(name = "idx_test_cases_problem_active", columnList = "problem_id, active"),
                @Index(name = "idx_test_cases_problem_id", columnList = "problem_id")
        }
)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CodingTestCase {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(
            name = "problem_id",
            nullable = false
    )
    private CodingProblem problem;

    @Column(nullable = false)
    private Integer testCaseNumber;

    @Column(
            nullable = false,
            columnDefinition = "LONGTEXT"
    )
    private String input;

    @Column(
            nullable = false,
            columnDefinition = "LONGTEXT"
    )
    private String expectedOutput;

    @Builder.Default
    @Column(nullable = false)
    private boolean hidden = false;

    @Builder.Default
    @Column(nullable = false)
    private boolean active = true;
}