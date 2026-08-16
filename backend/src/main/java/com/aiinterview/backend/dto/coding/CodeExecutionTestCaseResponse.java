package com.aiinterview.backend.dto.coding;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CodeExecutionTestCaseResponse {

    private Integer testCaseNumber;

    private boolean passed;

    private String input;

    private String expectedOutput;

    private String actualOutput;

    private String error;

    private Long runtime;

    private String status;
}