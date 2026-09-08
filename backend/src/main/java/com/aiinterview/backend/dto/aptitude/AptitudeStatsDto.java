package com.aiinterview.backend.dto.aptitude;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AptitudeStatsDto {
    private long totalQuestions;
    private Map<String, Long> categoryCounts;
    private Map<String, Long> topicCounts;
    private Map<String, Long> difficultyCounts;
    private Map<String, Long> attributionCounts;
    private long duplicateCount;
    private long malformedCount;
}
