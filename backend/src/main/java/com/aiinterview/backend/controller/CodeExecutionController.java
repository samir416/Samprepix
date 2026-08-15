package com.aiinterview.backend.controller;

import com.aiinterview.backend.dto.coding.CodeExecutionRequest;
import com.aiinterview.backend.dto.coding.CodeExecutionResponse;
import com.aiinterview.backend.service.coding.CodeExecutionService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/coding")
public class CodeExecutionController {

    private final CodeExecutionService codeExecutionService;

    public CodeExecutionController(
            CodeExecutionService codeExecutionService
    ) {
        this.codeExecutionService = codeExecutionService;
    }

    @PostMapping("/execute")
    public ResponseEntity<CodeExecutionResponse> executeCode(
            @RequestBody CodeExecutionRequest request
    ) {

        CodeExecutionResponse response =
                codeExecutionService.execute(request);

        return ResponseEntity.ok(response);
    }
}