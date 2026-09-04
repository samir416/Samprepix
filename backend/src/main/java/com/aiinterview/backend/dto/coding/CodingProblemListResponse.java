package com.aiinterview.backend.dto.coding;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CodingProblemListResponse {

    private Long id;
    private String title;
    private String difficulty;
    private String category;
    private List<String> tags;
    private Integer minimumExperienceLevel;
    private boolean active;
}
