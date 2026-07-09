package com.aiinterview.backend.dto.groq;

import java.util.List;

public class GroqResponse {

    private List<Choice> choices;

    public GroqResponse() {
    }

    public List<Choice> getChoices() {
        return choices;
    }

    public void setChoices(List<Choice> choices) {
        this.choices = choices;
    }
}