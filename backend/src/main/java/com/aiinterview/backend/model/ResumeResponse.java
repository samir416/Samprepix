package com.aiinterview.backend.model;

import java.util.List;

public class ResumeResponse {

    private boolean githubFound;

    private boolean linkedinFound;

    private boolean projectFound;

    private boolean educationFound;

    private boolean experienceFound;

    private int score;

    private List<String> skills;

    private List<String> missingSkills;

    private List<String> suggestions;

    public int getScore() {
        return score;
    }

    public void setScore(int score) {
        this.score = score;
    }

    public List<String> getSkills() {
        return skills;
    }

    public void setSkills(List<String> skills) {
        this.skills = skills;
    }

    public List<String> getMissingSkills() {
        return missingSkills;
    }

    public void setMissingSkills(List<String> missingSkills) {
        this.missingSkills = missingSkills;
    }

    public List<String> getSuggestions() {
        return suggestions;
    }

    public void setSuggestions(List<String> suggestions) {
        this.suggestions = suggestions;
    }

    public boolean isGithubFound() {
    return githubFound;
}

public void setGithubFound(boolean githubFound) {
    this.githubFound = githubFound;
}

public boolean isLinkedinFound() {
    return linkedinFound;
}

public void setLinkedinFound(boolean linkedinFound) {
    this.linkedinFound = linkedinFound;
}

public boolean isProjectFound() {
    return projectFound;
}

public void setProjectFound(boolean projectFound) {
    this.projectFound = projectFound;
}

public boolean isEducationFound() {
    return educationFound;
}

public void setEducationFound(boolean educationFound) {
    this.educationFound = educationFound;
}

public boolean isExperienceFound() {
    return experienceFound;
}

public void setExperienceFound(boolean experienceFound) {
    this.experienceFound = experienceFound;
}

    
}