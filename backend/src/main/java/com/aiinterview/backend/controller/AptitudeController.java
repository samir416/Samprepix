package com.aiinterview.backend.controller;

import com.aiinterview.backend.dto.aptitude.*;
import com.aiinterview.backend.entity.User;
import com.aiinterview.backend.repository.UserRepository;
import com.aiinterview.backend.service.aptitude.AptitudeQuestionService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/aptitude")
@RequiredArgsConstructor
public class AptitudeController {

    private final AptitudeQuestionService aptitudeQuestionService;
    private final UserRepository userRepository;

    @GetMapping("/questions")
    public ResponseEntity<Page<AptitudeQuestionDto>> getQuestionsByTopic(
            @RequestParam String topicId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        Page<AptitudeQuestionDto> result = aptitudeQuestionService.getQuestionsByTopic(
                topicId, PageRequest.of(Math.max(0, page), Math.min(100, Math.max(1, size)), Sort.by("id").ascending())
        );
        return ResponseEntity.ok(result);
    }

    @GetMapping("/questions/{id}")
    public ResponseEntity<AptitudeQuestionDto> getQuestionById(@PathVariable Long id) {
        return ResponseEntity.ok(aptitudeQuestionService.getQuestionById(id));
    }

    @GetMapping("/category")
    public ResponseEntity<Page<AptitudeQuestionDto>> getQuestionsByCategory(
            @RequestParam String name,
            @RequestParam(required = false) String difficulty,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        Page<AptitudeQuestionDto> result = aptitudeQuestionService.getQuestionsByCategory(
                name, difficulty, PageRequest.of(Math.max(0, page), Math.min(100, Math.max(1, size)), Sort.by("id").ascending())
        );
        return ResponseEntity.ok(result);
    }

    @PostMapping("/check")
    public ResponseEntity<CheckAnswerResponse> checkAnswer(@RequestBody CheckAnswerRequest request) {
        return ResponseEntity.ok(aptitudeQuestionService.checkAnswer(request));
    }

    @GetMapping("/stats")
    public ResponseEntity<AptitudeStatsDto> getStats() {
        return ResponseEntity.ok(aptitudeQuestionService.getAptitudeStats());
    }

    @PostMapping("/seed")
    public ResponseEntity<String> triggerSeed() {
        aptitudeQuestionService.seedQuestionBankIfNotPresent();
        return ResponseEntity.ok("Seeding verified. Total questions: " + aptitudeQuestionService.getAptitudeStats().getTotalQuestions());
    }

    @GetMapping("/assessment")
    public ResponseEntity<List<AptitudeAssessmentQuestionDto>> generateAssessment(
            @RequestParam(defaultValue = "all") String track,
            @RequestParam(defaultValue = "15") int count
    ) {
        List<AptitudeAssessmentQuestionDto> questions = aptitudeQuestionService.generateAssessment(track, count);
        return ResponseEntity.ok(questions);
    }

    @PostMapping("/assessment/submit")
    public ResponseEntity<AptitudeAttemptResponse> submitAssessment(
            Authentication authentication,
            @RequestBody AptitudeSubmitRequest request
    ) {
        User user = resolveUser(authentication);
        AptitudeAttemptResponse response = aptitudeQuestionService.submitAssessment(user, request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/attempts")
    public ResponseEntity<List<AptitudeAttemptResponse>> getUserAttempts(Authentication authentication) {
        User user = resolveUser(authentication);
        List<AptitudeAttemptResponse> attempts = aptitudeQuestionService.getUserAttempts(user);
        return ResponseEntity.ok(attempts);
    }

    @GetMapping("/attempts/{id}")
    public ResponseEntity<AptitudeAttemptResponse> getAttemptById(
            Authentication authentication,
            @PathVariable Long id
    ) {
        User user = resolveUser(authentication);
        AptitudeAttemptResponse attempt = aptitudeQuestionService.getAttemptById(user, id);
        return ResponseEntity.ok(attempt);
    }

    private User resolveUser(Authentication authentication) {
        if (authentication != null && authentication.getName() != null) {
            Optional<User> optionalUser = userRepository.findByEmail(authentication.getName());
            if (optionalUser.isPresent()) {
                return optionalUser.get();
            }
            optionalUser = userRepository.findByUsername(authentication.getName());
            if (optionalUser.isPresent()) {
                return optionalUser.get();
            }
        }
        return userRepository.findAll().stream().findFirst()
                .orElseThrow(() -> new RuntimeException("No registered user found"));
    }
}
