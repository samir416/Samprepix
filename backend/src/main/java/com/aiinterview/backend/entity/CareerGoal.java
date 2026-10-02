package com.aiinterview.backend.entity;

import com.fasterxml.jackson.annotation.JsonCreator;

public enum CareerGoal {

    JOB,

    COMPANY_SWITCH,

    DOMAIN_SWITCH,

    PROMOTION,

    INTERVIEW_PRACTICE;

    @JsonCreator
    public static CareerGoal fromString(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        String normalized = value.trim().toUpperCase().replace("-", "_").replace(" ", "_");
        if (normalized.equals("GET_FIRST_JOB") || normalized.equals("FIRST_JOB")) {
            return JOB;
        }
        if (normalized.equals("SWITCH_COMPANY")) {
            return COMPANY_SWITCH;
        }
        if (normalized.equals("SWITCH_DOMAIN")) {
            return DOMAIN_SWITCH;
        }
        if (normalized.equals("GET_PROMOTION")) {
            return PROMOTION;
        }
        for (CareerGoal goal : values()) {
            if (goal.name().equalsIgnoreCase(normalized)) {
                return goal;
            }
        }
        return null;
    }

}