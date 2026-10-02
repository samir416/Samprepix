package com.aiinterview.backend.dto.support;

import java.util.List;
import java.util.Map;

public class SupportQuestionRequest {

    private String question;
    private List<Map<String, String>> history;

    public SupportQuestionRequest() {
    }

    public SupportQuestionRequest(String question) {
        this.question = question;
    }

    public SupportQuestionRequest(String question, List<Map<String, String>> history) {
        this.question = question;
        this.history = history;
    }

    public String getQuestion() {
        return question;
    }

    public void setQuestion(String question) {
        this.question = question;
    }

    public List<Map<String, String>> getHistory() {
        return history;
    }

    public void setHistory(List<Map<String, String>> history) {
        this.history = history;
    }
}

