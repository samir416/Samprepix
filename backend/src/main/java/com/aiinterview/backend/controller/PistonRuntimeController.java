package com.aiinterview.backend.controller;

import com.aiinterview.backend.service.coding.PistonRuntimeService;
import com.aiinterview.backend.service.coding.PistonRuntimeService.PistonRuntime;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/coding/runtimes")
public class PistonRuntimeController {

    private final PistonRuntimeService pistonRuntimeService;

    public PistonRuntimeController(
            PistonRuntimeService pistonRuntimeService
    ) {
        this.pistonRuntimeService =
                pistonRuntimeService;
    }

    @GetMapping
    public ResponseEntity<List<PistonRuntime>> getRuntimes() {

        return ResponseEntity.ok(
                pistonRuntimeService.getAvailableLanguages()
        );
    }
}