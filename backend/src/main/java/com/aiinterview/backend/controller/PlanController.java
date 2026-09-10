package com.aiinterview.backend.controller;

import com.aiinterview.backend.dto.admin.*;
import com.aiinterview.backend.entity.Plan;
import com.aiinterview.backend.repository.PlanRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/plans")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class PlanController {

    private final PlanRepository planRepository;

    @GetMapping
    public ResponseEntity<List<PlanResponse>> getAllPlans() {
        List<Plan> plans = planRepository.findAll();
        return ResponseEntity.ok(plans.stream().map(PlanResponse::fromEntity).toList());
    }

    @GetMapping("/active")
    public ResponseEntity<List<PlanResponse>> getActivePlans() {
        List<Plan> plans = planRepository.findByActiveTrue();
        return ResponseEntity.ok(plans.stream().map(PlanResponse::fromEntity).toList());
    }

    @GetMapping("/{id}")
    public ResponseEntity<PlanResponse> getPlanById(@PathVariable Long id) {
        Plan plan = planRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Plan not found"));
        return ResponseEntity.ok(PlanResponse.fromEntity(plan));
    }

    @PostMapping
    public ResponseEntity<PlanResponse> createPlan(@Valid @RequestBody PlanRequest request) {
        if (planRepository.existsByName(request.getName())) {
            throw new RuntimeException("Plan already exists");
        }
        Plan plan = Plan.builder()
                .name(request.getName())
                .description(request.getDescription())
                .priceInr(request.getPriceInr())
                .priceUsd(request.getPriceUsd())
                .interval(request.getInterval())
                .maxMockInterviews(request.getMaxMockInterviews())
                .maxResumeScans(request.getMaxResumeScans())
                .maxCodingProblems(request.getMaxCodingProblems())
                .maxAptitudeQuestions(request.getMaxAptitudeQuestions())
                .includesAIHints(request.isIncludesAIHints())
                .includesAnalytics(request.isIncludesAnalytics())
                .includesTier1Companies(request.isIncludesTier1Companies())
                .includesPriorityCompute(request.isIncludesPriorityCompute())
                .active(request.isActive())
                .featured(request.isFeatured())
                .build();
        planRepository.save(plan);
        return ResponseEntity.ok(PlanResponse.fromEntity(plan));
    }

    @PutMapping("/{id}")
    public ResponseEntity<PlanResponse> updatePlan(
            @PathVariable Long id,
            @Valid @RequestBody PlanRequest request) {
        Plan plan = planRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Plan not found"));
        plan.setName(request.getName());
        plan.setDescription(request.getDescription());
        plan.setPriceInr(request.getPriceInr());
        plan.setPriceUsd(request.getPriceUsd());
        plan.setInterval(request.getInterval());
        plan.setMaxMockInterviews(request.getMaxMockInterviews());
        plan.setMaxResumeScans(request.getMaxResumeScans());
        plan.setMaxCodingProblems(request.getMaxCodingProblems());
        plan.setMaxAptitudeQuestions(request.getMaxAptitudeQuestions());
        plan.setIncludesAIHints(request.isIncludesAIHints());
        plan.setIncludesAnalytics(request.isIncludesAnalytics());
        plan.setIncludesTier1Companies(request.isIncludesTier1Companies());
        plan.setIncludesPriorityCompute(request.isIncludesPriorityCompute());
        plan.setActive(request.isActive());
        plan.setFeatured(request.isFeatured());
        planRepository.save(plan);
        return ResponseEntity.ok(PlanResponse.fromEntity(plan));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deletePlan(@PathVariable Long id) {
        planRepository.deleteById(id);
        return ResponseEntity.ok(Map.of("message", "Plan deleted successfully"));
    }
}
