package com.aiinterview.backend.dto.interview;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InterviewQuestionRequest {

    @NotNull(message = "Session id is required.")
    private Long sessionId;

    @NotBlank(message = "Answer is required.")
    private String answer;

}