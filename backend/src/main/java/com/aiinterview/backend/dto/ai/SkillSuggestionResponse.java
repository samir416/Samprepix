package com.aiinterview.backend.dto.ai;

import java.util.List;

public class SkillSuggestionResponse {

    private List<String> skills;

    public SkillSuggestionResponse() {
    }

    public List<String> getSkills() {

        return skills;

    }

    public void setSkills(

            List<String> skills

    ) {

        this.skills = skills;

    }

}