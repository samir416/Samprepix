package com.aiinterview.backend.controller;

import com.aiinterview.backend.entity.CodingProblem;
import com.aiinterview.backend.service.coding.CodingProblemGithubImportService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/coding/github")
public class CodingProblemGithubController {

    private final CodingProblemGithubImportService
            codingProblemGithubImportService;

    public CodingProblemGithubController(
            CodingProblemGithubImportService
                    codingProblemGithubImportService
    ) {
        this.codingProblemGithubImportService =
                codingProblemGithubImportService;
    }

    @PostMapping("/import")
    public ResponseEntity<CodingProblem> importProblem(
            @RequestParam String repository,
            @RequestParam String problemPath
    ) {

        CodingProblem problem =
                codingProblemGithubImportService
                        .importProblem(
                                repository,
                                problemPath
                        );

        return ResponseEntity.ok(problem);
    }
}