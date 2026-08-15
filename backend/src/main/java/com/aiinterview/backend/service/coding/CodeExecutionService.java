package com.aiinterview.backend.service.coding;

import com.aiinterview.backend.dto.coding.CodeExecutionRequest;
import com.aiinterview.backend.dto.coding.CodeExecutionResponse;

public interface CodeExecutionService {

    CodeExecutionResponse execute(
            CodeExecutionRequest request
    );
}