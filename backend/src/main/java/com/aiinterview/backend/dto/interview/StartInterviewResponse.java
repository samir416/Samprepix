package com.aiinterview.backend.dto.interview;

import java.time.LocalDateTime;

public class StartInterviewResponse {

    private Long sessionId;
    private String status;
    private String message;
    private String firstQuestion;
    private LocalDateTime startedAt;

    public StartInterviewResponse() {
    }

    public StartInterviewResponse(Long sessionId,
                                  String status,
                                  String message,
                                  String firstQuestion,
                                  LocalDateTime startedAt) {
        this.sessionId = sessionId;
        this.status = status;
        this.message = message;
        this.firstQuestion = firstQuestion;
        this.startedAt = startedAt;
    }

    public Long getSessionId() {
        return sessionId;
    }

    public void setSessionId(Long sessionId) {
        this.sessionId = sessionId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getFirstQuestion() {
        return firstQuestion;
    }

    public void setFirstQuestion(String firstQuestion) {
        this.firstQuestion = firstQuestion;
    }

    public LocalDateTime getStartedAt() {
        return startedAt;
    }

    public void setStartedAt(LocalDateTime startedAt) {
        this.startedAt = startedAt;
    }
}