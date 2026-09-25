package com.aiinterview.backend.dto.support;

public class SupportQuestionRequest {

    private String question;

    public SupportQuestionRequest() {
    }

    public SupportQuestionRequest(String question) {
        this.question = question;
    }

    public String getQuestion() {
        return question;
    }

    public void setQuestion(String question) {
        this.question = question;
    }
}
