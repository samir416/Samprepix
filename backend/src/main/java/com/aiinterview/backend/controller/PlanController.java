package com.aiinterview.backend.controller;

import com.aiinterview.backend.dto.admin.PlanRequest;
import com.aiinterview.backend.dto.admin.PlanResponse;
import com.aiinterview.backend.entity.Plan;
import com.aiinterview.backend.repository.PlanRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Pattern;

@RestController
@RequestMapping("/api/admin/plans")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class PlanController {

    private final PlanRepository planRepository;

    private static final Pattern PLAN_NAME_PATTERN =
            Pattern.compile("[A-Z0-9_-]{1,30}");

    private static final Pattern INTERVAL_PATTERN =
            Pattern.compile("MONTH");

    // =========================================================
    // GET ALL PLANS
    // =========================================================

    @GetMapping({"", "/"})
    public ResponseEntity<List<PlanResponse>> getAllPlans() {

        List<Plan> plans = planRepository.findAll();

        return ResponseEntity.ok(
                plans.stream()
                        .map(PlanResponse::fromEntity)
                        .toList()
        );
    }

    // =========================================================
    // GET ACTIVE PLANS
    // =========================================================

    @GetMapping({"/active", "/active/"})
    public ResponseEntity<List<PlanResponse>> getActivePlans() {

        List<Plan> plans =
                planRepository.findByActiveTrueOrderByIdAsc();

        return ResponseEntity.ok(
                plans.stream()
                        .map(PlanResponse::fromEntity)
                        .toList()
        );
    }

    // =========================================================
    // GET PLAN BY ID
    // =========================================================

    @GetMapping("/{id}")
    public ResponseEntity<PlanResponse> getPlanById(
            @PathVariable Long id) {

        Plan plan = planRepository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException("Plan not found")
                );

        return ResponseEntity.ok(
                PlanResponse.fromEntity(plan)
        );
    }

    // =========================================================
    // CREATE PLAN
    // =========================================================

    @PostMapping({"", "/"})
    public ResponseEntity<PlanResponse> createPlan(
            @Valid @RequestBody PlanRequest request) {

        validatePlanRequest(request);

        String name =
                normalizePlanName(request.getName());

        String interval =
                normalizeInterval(request.getInterval());

        if (planRepository.existsByNameIgnoreCase(name)) {
            throw new IllegalArgumentException("Plan already exists");
        }

        Plan plan = Plan.builder()
                .name(name)
                .description(
                        request.getDescription().trim()
                )
                .priceInr(request.getPriceInr())
                .priceUsd(request.getPriceUsd())
                .interval(interval)
                .maxMockInterviews(
                        request.getMaxMockInterviews()
                )
                .maxResumeScans(
                        request.getMaxResumeScans()
                )
                .maxCodingProblems(
                        request.getMaxCodingProblems()
                )
                .maxAptitudeQuestions(
                        request.getMaxAptitudeQuestions()
                )
                .includesAIHints(
                        request.isIncludesAIHints()
                )
                .includesAnalytics(
                        request.isIncludesAnalytics()
                )
                .includesTier1Companies(
                        request.isIncludesTier1Companies()
                )
                .includesPriorityCompute(
                        request.isIncludesPriorityCompute()
                )
                .active(request.isActive())
                .featured(request.isFeatured())
                .build();

        Plan saved =
                planRepository.save(plan);

        return ResponseEntity.ok(
                PlanResponse.fromEntity(saved)
        );
    }

    // =========================================================
    // UPDATE PLAN
    // =========================================================

    @PutMapping("/{id}")
    public ResponseEntity<PlanResponse> updatePlan(
            @PathVariable Long id,
            @Valid @RequestBody PlanRequest request) {

        validatePlanRequest(request);

        Plan plan =
                planRepository.findById(id)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Plan not found"
                                )
                        );

        String name =
                normalizePlanName(request.getName());

        String interval =
                normalizeInterval(request.getInterval());

        planRepository.findByNameIgnoreCase(name)
                .filter(existing -> !existing.getId().equals(id))
                .ifPresent(existing -> {
                    throw new IllegalArgumentException(
                            "Another plan already exists with name: " + name
                    );
                });

        plan.setName(name);
        plan.setDescription(
                request.getDescription().trim()
        );
        plan.setPriceInr(
                request.getPriceInr()
        );
        plan.setPriceUsd(
                request.getPriceUsd()
        );
        plan.setInterval(interval);

        plan.setMaxMockInterviews(
                request.getMaxMockInterviews()
        );
        plan.setMaxResumeScans(
                request.getMaxResumeScans()
        );
        plan.setMaxCodingProblems(
                request.getMaxCodingProblems()
        );
        plan.setMaxAptitudeQuestions(
                request.getMaxAptitudeQuestions()
        );

        plan.setIncludesAIHints(
                request.isIncludesAIHints()
        );
        plan.setIncludesAnalytics(
                request.isIncludesAnalytics()
        );
        plan.setIncludesTier1Companies(
                request.isIncludesTier1Companies()
        );
        plan.setIncludesPriorityCompute(
                request.isIncludesPriorityCompute()
        );

        plan.setActive(
                request.isActive()
        );
        plan.setFeatured(
                request.isFeatured()
        );

        Plan saved =
                planRepository.save(plan);

        return ResponseEntity.ok(
                PlanResponse.fromEntity(saved)
        );
    }

    // =========================================================
    // DELETE PLAN
    // =========================================================

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deletePlan(
            @PathVariable Long id) {

        Plan plan =
                planRepository.findById(id)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Plan not found"
                                )
                        );

        String planName =
                plan.getName() == null
                        ? ""
                        : plan.getName().trim();

        /*
         * Core plans must never be physically deleted because
         * subscriptions, payments and invoices may reference them.
         */
        if ("STARTER".equalsIgnoreCase(planName)
                || "PRO".equalsIgnoreCase(planName)
                || "ELITE".equalsIgnoreCase(planName)) {

            return ResponseEntity
                    .badRequest()
                    .body(
                            Map.of(
                                    "message",
                                    "Core plans cannot be deleted. Deactivate the plan instead."
                            )
                    );
        }

        planRepository.delete(plan);

        return ResponseEntity.ok(
                Map.of(
                        "message",
                        "Plan deleted successfully"
                )
        );
    }

    // =========================================================
    // VALIDATION
    // =========================================================

    private void validatePlanRequest(
            PlanRequest request) {

        if (request == null) {
            throw new IllegalArgumentException(
                    "Plan request is required"
            );
        }

        if (request.getName() == null
                || request.getName().isBlank()) {

            throw new IllegalArgumentException(
                    "Plan name is required"
            );
        }

        if (request.getDescription() == null
                || request.getDescription().isBlank()
                || request.getDescription().trim().length() > 50) {

            throw new IllegalArgumentException(
                    "Plan description is required and must be 50 characters or less"
            );
        }

        if (!Double.isFinite(request.getPriceInr())
                || request.getPriceInr() < 0
                || request.getPriceInr() > 1_000_000) {

            throw new IllegalArgumentException(
                    "Invalid INR price"
            );
        }

        if (!Double.isFinite(request.getPriceUsd())
                || request.getPriceUsd() < 0
                || request.getPriceUsd() > 100_000) {

            throw new IllegalArgumentException(
                    "Invalid USD price"
            );
        }

        if (request.getMaxMockInterviews() < 0
                || request.getMaxResumeScans() < 0
                || request.getMaxCodingProblems() < 0
                || request.getMaxAptitudeQuestions() < 0
                || request.getMaxMockInterviews() > 1_000_000
                || request.getMaxResumeScans() > 1_000_000
                || request.getMaxCodingProblems() > 1_000_000
                || request.getMaxAptitudeQuestions() > 1_000_000) {

            throw new IllegalArgumentException(
                    "Plan limits are invalid"
            );
        }

        if (request.getInterval() == null
                || request.getInterval().isBlank()) {

            throw new IllegalArgumentException(
                    "Plan interval is required"
            );
        }
    }

    // =========================================================
    // NORMALIZATION
    // =========================================================

    private String normalizePlanName(
            String name) {

        if (name == null
                || name.isBlank()) {

            throw new IllegalArgumentException(
                    "Plan name is required"
            );
        }

        String normalized = name.trim()
                .toUpperCase(Locale.ROOT);

        if (!PLAN_NAME_PATTERN.matcher(normalized).matches()) {
            throw new IllegalArgumentException("Invalid plan name");
        }

        return normalized;
    }

    private String normalizeInterval(
            String interval) {

        if (interval == null
                || interval.isBlank()) {

            throw new IllegalArgumentException(
                    "Plan interval is required"
            );
        }

        String normalized = interval.trim()
                .toUpperCase(Locale.ROOT);

        if (!INTERVAL_PATTERN.matcher(normalized).matches()) {
            throw new IllegalArgumentException("Only monthly plans are supported");
        }

        return normalized;
    }
}