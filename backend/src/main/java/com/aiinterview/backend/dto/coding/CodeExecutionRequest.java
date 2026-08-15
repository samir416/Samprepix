package com.aiinterview.backend.dto.coding;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CodeExecutionRequest {

    private Long problemId;

    private String language;

    private String code;
}