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
public class InterviewProgressResponse {

    private Long sessionId;

    private String status;

    private Integer questionsAnswered;

    private Integer overallScore;

    private Integer technicalAccuracy;

    private Integer completeness;

    private Integer communication;

    private String currentQuestion;

    private String targetRole;

    private String experienceLevel;

    private List<String> skills;

    private Boolean interviewEndedByUser;

    private Boolean reportGenerated;

    private LocalDateTime startedAt;

    private LocalDateTime completedAt;

}