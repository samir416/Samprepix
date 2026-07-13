package com.aiinterview.backend.service.adapter;

import com.aiinterview.backend.dto.ai.AIResponse;
import com.aiinterview.backend.model.ResumeResponse;
import org.springframework.stereotype.Component;

@Component
public class AIResponseAdapter {

    public ResumeResponse convert(AIResponse aiResponse) {

        ResumeResponse response = new ResumeResponse();

        response.setRole(aiResponse.getRole());

        response.setScore(
                aiResponse.getAtsScore() == null
                        ? 0
                        : aiResponse.getAtsScore()
        );

        response.setSkills(aiResponse.getDetectedSkills());

        response.setMissingSkills(aiResponse.getMissingSkills());

        response.setSuggestions(aiResponse.getSuggestions());

        response.setValidResume(true);

        response.setGithubFound(false);

        response.setLinkedinFound(false);

        response.setProjectFound(false);

        response.setEducationFound(false);

        response.setExperienceFound(false);

        response.setRejectionReason(null);

        return response;
    }
}