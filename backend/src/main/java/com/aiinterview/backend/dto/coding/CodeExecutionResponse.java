package com.aiinterview.backend.dto.coding;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CodeExecutionResponse {

    private String status;

    private boolean passed;

    private Integer totalTests;

    private Integer passedTests;

    private Integer failedTests;

    private Long runtime;

    private String output;

    private String expectedOutput;

    private String error;

    private String message;
}