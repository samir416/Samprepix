package com.aiinterview.backend.entity;

import com.fasterxml.jackson.annotation.JsonCreator;

public enum JourneyType {

    STUDENT,

    WORKING_PROFESSIONAL;

    @JsonCreator
    public static JourneyType fromString(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        String normalized = value.trim().toUpperCase().replace("-", "_").replace(" ", "_");
        for (JourneyType jt : values()) {
            if (jt.name().equalsIgnoreCase(normalized)) {
                return jt;
            }
        }
        return null;
    }

}