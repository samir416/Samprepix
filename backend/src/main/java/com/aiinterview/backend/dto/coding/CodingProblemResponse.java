package com.aiinterview.backend.dto.coding;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CodingProblemResponse {

    private Long id;

    private String title;

    private String description;

    private String difficulty;
    private String category;

    private List<String> tags;

    private String inputExample;

    private String outputExample;

    private List<String> constraints;

    private Integer minimumExperienceLevel;

    private boolean active;

    private Map<String, String> starterCodes;

    private Map<String, Object> languageConfigurations;

    private List<CodingPublicTestCaseResponse> testCases;
}