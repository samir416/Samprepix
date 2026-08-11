package com.aiinterview.backend.dto.interview;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InterviewQuestionResponse {

    private String evaluation;

    private String nextQuestion;

    private Integer questionNumber;

    private Integer score;

    private Integer technicalAccuracy;

private Integer completeness;

private Integer communication;

    private Boolean interviewCompleted;

}