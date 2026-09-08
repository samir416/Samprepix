package com.aiinterview.backend.dto.aptitude;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CheckAnswerResponse {
    private Long questionId;
    private boolean correct;
    private String correctOption;
    private String explanation;
    private String formulaHint;
}
