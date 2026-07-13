package com.aiinterview.backend.dto.interview;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

public class StartInterviewRequest {

    @NotBlank(message = "Interview type is required")
    private String interviewType;

    @Min(value = 1, message = "Total questions must be at least 1")
    @Max(value = 20, message = "Maximum 20 questions are allowed")
    private Integer totalQuestions;

    public String getInterviewType() {
        return interviewType;
    }

    public void setInterviewType(String interviewType) {
        this.interviewType = interviewType;
    }

    public Integer getTotalQuestions() {
        return totalQuestions;
    }

    public void setTotalQuestions(Integer totalQuestions) {
        this.totalQuestions = totalQuestions;
    }
}