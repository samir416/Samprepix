package com.aiinterview.backend.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "aptitude_questions",
        indexes = {
                @Index(name = "idx_aptitude_category", columnList = "category"),
                @Index(name = "idx_aptitude_topic_id", columnList = "topic_id"),
                @Index(name = "idx_aptitude_difficulty", columnList = "difficulty"),
                @Index(name = "uk_aptitude_question_code", columnList = "question_code", unique = true)
        }
)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AptitudeQuestion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "question_code", nullable = false, length = 64, unique = true)
    private String questionCode;

    @Column(nullable = false, length = 64)
    private String category; // "Quantitative Aptitude", "Logical Reasoning", "Verbal Ability", "Data Interpretation"

    @Column(name = "topic_id", nullable = false, length = 64)
    private String topicId; // e.g. "quant-1", "logic-2", etc.

    @Column(nullable = false, length = 128)
    private String topic; // e.g. "Percentages, Profit & Loss"

    @Column(nullable = false, length = 20)
    private String difficulty; // "EASY", "MEDIUM", "HARD"

    @Column(name = "question_text", nullable = false, columnDefinition = "TEXT")
    private String questionText;

    @Column(name = "option_a", nullable = false, columnDefinition = "TEXT")
    private String optionA;

    @Column(name = "option_b", nullable = false, columnDefinition = "TEXT")
    private String optionB;

    @Column(name = "option_c", nullable = false, columnDefinition = "TEXT")
    private String optionC;

    @Column(name = "option_d", nullable = false, columnDefinition = "TEXT")
    private String optionD;

    @Column(name = "correct_option", nullable = false, length = 5)
    private String correctOption; // "A", "B", "C", "D"

    @Column(nullable = false, columnDefinition = "TEXT")
    private String explanation;

    @Column(name = "formula_hint", columnDefinition = "TEXT")
    private String formulaHint;

    @Column(name = "source_attribution", length = 150)
    private String sourceAttribution; // e.g. "TCS NQT Placement", "Infosys InfyTQ Pattern", etc.

    @Column(length = 150)
    private String tags;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    public void prePersist() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }
}
