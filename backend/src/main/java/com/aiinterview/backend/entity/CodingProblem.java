package com.aiinterview.backend.entity;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.BatchSize;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(
        name = "coding_problems",
        indexes = {
                @Index(name = "idx_coding_problem_active", columnList = "active"),
                @Index(name = "idx_coding_problem_difficulty_active", columnList = "difficulty, active"),
                @Index(name = "idx_coding_problem_title", columnList = "title"),
                @Index(name = "idx_coding_problem_category", columnList = "category"),
                @Index(name = "uk_coding_problem_slug", columnList = "slug", unique = true),
                @Index(name = "uk_coding_problem_source_id", columnList = "source_id", unique = true)
        }
)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CodingProblem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

        @Column(length = 180)
        private String slug;

        @Column(name = "source_id", length = 180)
        private String sourceId;

    @Column(nullable = false, length = 150)
    private String title;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false, length = 20)
    private String difficulty;

    @Column(length = 50)
    @Builder.Default
    private String category = "DSA";

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(
            name = "coding_problem_tags",
            joinColumns = @JoinColumn(name = "problem_id")
    )
    @Column(name = "tag", length = 100)
    @Builder.Default
        @BatchSize(size = 50)
    private List<String> tags = new ArrayList<>();

    @Column(columnDefinition = "TEXT")
    private String inputExample;

    @Column(columnDefinition = "TEXT")
    private String outputExample;

    @ElementCollection
    @CollectionTable(
            name = "coding_problem_constraints",
            joinColumns = @JoinColumn(name = "problem_id")
    )
    @Column(
            name = "constraint_text",
            columnDefinition = "TEXT"
    )
    @Builder.Default
    private List<String> constraints =
            new ArrayList<>();

    @Column(columnDefinition = "LONGTEXT")
    private String starterCode;

    @Column(columnDefinition = "LONGTEXT")
    private String languageConfigurations;

    @Column(length = 100)
    private String functionName;

    @Column(columnDefinition = "TEXT")
    private String functionSignature;

    @Column(length = 100)
    private String returnType;

    @Column(columnDefinition = "TEXT")
    private String parameterTypes;

    @Column(nullable = false)
    private Integer minimumExperienceLevel;

    @Column(nullable = false)
        @Builder.Default
    private boolean active = true;
}