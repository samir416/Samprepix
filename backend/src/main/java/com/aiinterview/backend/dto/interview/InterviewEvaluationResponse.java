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
public class InterviewEvaluationResponse {

    private Integer technicalAccuracy;

    private Integer completeness;

    private Integer communication;

    private Integer overallScore;

    private String performance;

    private String idealAnswer;

    private String feedback;

    private String nextFocusSkill;

    private String difficulty;

    private List<String> strengths;

    private List<String> missingConcepts;

}