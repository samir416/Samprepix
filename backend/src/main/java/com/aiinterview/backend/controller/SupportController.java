package com.aiinterview.backend.controller;

import com.aiinterview.backend.dto.support.ReportProblemRequest;
import com.aiinterview.backend.dto.support.SupportQuestionRequest;
import com.aiinterview.backend.dto.support.SupportQuestionResponse;
import com.aiinterview.backend.service.SupportService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.Map;

@RestController
@RequestMapping("/api/support")
@CrossOrigin(origins = "*")
public class SupportController {

    private final SupportService supportService;

    public SupportController(SupportService supportService) {
        this.supportService = supportService;
    }

    @PostMapping("/report-problem")
    public ResponseEntity<?> reportProblem(
            @RequestBody ReportProblemRequest request,
            Principal principal
    ) {
        String username = principal != null ? principal.getName() : null;
        supportService.submitProblemReport(request, username);
        return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Thank you. Your problem report has been successfully dispatched to the platform administration team."
        ));
    }

    @PostMapping("/ask")
    public ResponseEntity<SupportQuestionResponse> askQuestion(
            @RequestBody SupportQuestionRequest request
    ) {
        String question = request != null ? request.getQuestion() : "";
        SupportQuestionResponse response = supportService.answerQuestion(question);
        return ResponseEntity.ok(response);
    }
}
