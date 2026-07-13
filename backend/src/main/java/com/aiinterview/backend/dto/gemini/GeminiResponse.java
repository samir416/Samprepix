package com.aiinterview.backend.dto.gemini;

import java.util.List;

public class GeminiResponse {

    private List<Candidate> candidates;

    public GeminiResponse() {
    }

    public List<Candidate> getCandidates() {
        return candidates;
    }

    public void setCandidates(List<Candidate> candidates) {
        this.candidates = candidates;
    }
}