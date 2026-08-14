package com.aiinterview.backend.entity;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "coding_problems")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CodingProblem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 150)
    private String title;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false, length = 20)
    private String difficulty;

    @ElementCollection
    @CollectionTable(
            name = "coding_problem_tags",
            joinColumns = @JoinColumn(name = "problem_id")
    )
    @Column(name = "tag", length = 100)
    @Builder.Default
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
    @Column(name = "constraint_text", columnDefinition = "TEXT")
    @Builder.Default
    private List<String> constraints = new ArrayList<>();

    @Column(columnDefinition = "LONGTEXT")
    private String starterCode;

    @Column(nullable = false)
    private Integer minimumExperienceLevel;

    @Column(nullable = false)
    private boolean active = true;
}