package com.aiinterview.backend.dto.aptitude;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AptitudeAssessmentQuestionDto {
    private Long id;
    private String questionCode;
    private String category;
    private String topicId;
    private String topic;
    private String difficulty;
    private String questionText;
    private List<AptitudeOptionDto> options;
    private String formulaHint;
    private String sourceAttribution;
}
