package com.aiinterview.backend.dto.interview;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class InterviewQuestionRequest {

    @NotNull(message = "Session id is required")
    private Long sessionId;

    @NotBlank(message = "Answer is required")
    private String answer;

    public InterviewQuestionRequest() {
    }

    public Long getSessionId() {
        return sessionId;
    }

    public void setSessionId(Long sessionId) {
        this.sessionId = sessionId;
    }

    public String getAnswer() {
        return answer;
    }

    public void setAnswer(String answer) {
        this.answer = answer;
    }
}