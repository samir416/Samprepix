package com.aiinterview.backend.dto.aptitude;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AptitudeAttemptResponse {
    private Long id;
    private String trackId;
    private String trackTitle;
    private int totalQuestions;
    private int correctCount;
    private int incorrectCount;
    private int unansweredCount;
    private int score;
    private double percentage;
    private double accuracy;
    private int timeSpentSeconds;
    private int timeLimitSeconds;
    private String completionReason;
    private LocalDateTime completedAt;
    private boolean passed;
    private Map<String, Object> categoryBreakdown;
    private Map<String, Object> difficultyBreakdown;
    private List<QuestionReviewDto> questions;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class QuestionReviewDto {
        private Long id;
        private String questionCode;
        private String category;
        private String topic;
        private String difficulty;
        private String questionText;
        private List<AptitudeOptionDto> options;
        private String selectedOption;
        private String correctOption;
        private boolean isCorrect;
        private boolean isAnswered;
        private String explanation;
        private String formulaHint;
        private String sourceAttribution;
    }
}
