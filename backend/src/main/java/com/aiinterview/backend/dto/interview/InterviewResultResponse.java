package com.aiinterview.backend.dto.interview;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InterviewResultResponse {

    private Long sessionId;

    private String status;

    private String targetRole;

    private String experienceLevel;

    private List<String> skills;

    private Integer questionsAnswered;

    private Integer overallScore;

    private Integer technicalAccuracy;

    private Integer completeness;

    private Integer communication;

    private String nextFocusSkill;

    private String difficulty;

    private LocalDateTime startedAt;

    private LocalDateTime completedAt;

    private List<InterviewAnswerResponse> answers;

}