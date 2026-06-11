package com.aiinterview.backend.entity;

import jakarta.persistence.*;

@Entity
public class ResumeAnalysis {

    @Id
    @GeneratedValue(
            strategy =
                    GenerationType.IDENTITY
    )
    private Long id;

    private String userEmail;

    private int score;

    @Column(length = 2000)
    private String skills;

    @Column(length = 2000)
    private String missingSkills;

    @Column(length = 3000)
    private String suggestions;

    private String analyzedAt;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUserEmail() {
        return userEmail;
    }

    public void setUserEmail(
            String userEmail
    ) {
        this.userEmail = userEmail;
    }

    public int getScore() {
        return score;
    }

    public void setScore(int score) {
        this.score = score;
    }

    public String getSkills() {
        return skills;
    }

    public void setSkills(
            String skills
    ) {
        this.skills = skills;
    }

    public String getMissingSkills() {
        return missingSkills;
    }

    public void setMissingSkills(
            String missingSkills
    ) {
        this.missingSkills =
                missingSkills;
    }

    public String getSuggestions() {
        return suggestions;
    }

    public void setSuggestions(
            String suggestions
    ) {
        this.suggestions =
                suggestions;
    }

    public String getAnalyzedAt() {
        return analyzedAt;
    }

    public void setAnalyzedAt(
            String analyzedAt
    ) {
        this.analyzedAt =
                analyzedAt;
    }
}