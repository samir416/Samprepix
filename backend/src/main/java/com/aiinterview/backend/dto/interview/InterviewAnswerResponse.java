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
public class InterviewAnswerResponse {

    private Integer questionNumber;

    private String question;

    private String answer;

    private String evaluation;

    private Integer score;
}