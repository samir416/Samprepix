package com.aiinterview.backend.dto.interview;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InterviewAnswerResponse {

    private Integer questionNumber;

    private String question;

    private String answer;

    private Integer technicalAccuracy;

    private Integer completeness;

    private Integer communication;

    private Integer overallScore;

    private String performance;

    private String difficulty;

    private String idealAnswer;

    private String feedback;

    private List<String> strengths;

    private List<String> missingConcepts;
}