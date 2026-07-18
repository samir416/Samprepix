package com.aiinterview.backend.dto.interview;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InterviewProgressResponse {

    private Long sessionId;

    private String status;

    private Integer currentQuestion;

    private Integer totalQuestions;

    private Integer answeredQuestions;

    private Integer remainingQuestions;

    private Integer score;

    private Double percentage;
}