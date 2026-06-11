package com.aiinterview.backend.controller;

import com.aiinterview.backend.entity.ResumeAnalysis;
import com.aiinterview.backend.model.ResumeResponse;
import com.aiinterview.backend.service.ResumeService;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.util.List;

@RestController
@RequestMapping("/resume")
public class ResumeController {

    private final ResumeService resumeService;

    public ResumeController(
            ResumeService resumeService) {
        this.resumeService = resumeService;
    }

    @GetMapping("/analyze")
    public ResumeResponse analyzeResume() {

        return resumeService.analyzeResume();
    }

    @PostMapping("/upload")
    public ResponseEntity<String> uploadResume(

            @RequestParam("file") MultipartFile file) {

        if (file.isEmpty()) {

            return ResponseEntity
                    .badRequest()
                    .body("No file selected");
        }

        return ResponseEntity.ok(
                "File uploaded successfully: "
                        + file.getOriginalFilename());
    }

    @PostMapping("/extract")
    public ResponseEntity<String> extractText(

            @RequestParam("file") MultipartFile file

    ) throws Exception {

        return ResponseEntity.ok(

                resumeService
                        .extractTextFromPdf(file));
    }

    @PostMapping("/skills")
    public ResponseEntity<?> detectSkills(

            @RequestParam("file") MultipartFile file

    ) throws Exception {

        String text = resumeService
                .extractTextFromPdf(file);

        return ResponseEntity.ok(

                resumeService
                        .detectSkills(text));
    }

    @PostMapping("/score")
    public ResponseEntity<Integer> scoreResume(

            @RequestParam("file") MultipartFile file

    ) throws Exception {

        String text = resumeService
                .extractTextFromPdf(file);

        java.util.List<String> skills = resumeService
                .detectSkills(text);

        int score = resumeService
                .calculateScore(skills);

        return ResponseEntity.ok(score);
    }

    @PostMapping("/analyze-file")
    public ResumeResponse analyzeFile(

            @RequestParam("file") MultipartFile file

    ) throws Exception {

        return resumeService
                .analyzeResumeFile(file);
    }

    @GetMapping("/history")
    public List<ResumeAnalysis> history() {

        return resumeService
                .getHistory();
    }
}
