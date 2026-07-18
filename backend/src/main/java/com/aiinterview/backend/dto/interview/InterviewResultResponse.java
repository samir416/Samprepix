package com.aiinterview.backend.dto.interview;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InterviewResultResponse {

    private Long sessionId;

    private String status;

    private String interviewType;

    private Integer totalQuestions;

    private Integer score;

    private Double percentage;

    private String overallFeedback;

    private String strengths;

    private String weaknesses;

    private String suggestions;

    private List<InterviewAnswerResponse> answers;
}