package com.aiinterview.backend.service;

import com.aiinterview.backend.entity.Plan;
import com.aiinterview.backend.repository.PlanRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class PlanService {

    private final PlanRepository planRepository;

    @EventListener(ApplicationReadyEvent.class)
    @Transactional
    public void seedPlans() {
        createPlanIfMissing(
                "STARTER",
                "Starter Plan",
                0.0,
                0.0,
                3,
                5,
                10,
                20,
                false,
                false,
                false,
                false,
                true,
                true
        );

        createPlanIfMissing(
                "PRO",
                "Pro Plan",
                399.0,
                8.0,
                15,
                20,
                50,
                50,
                true,
                true,
                true,
                true,
                true,
                true
        );

        createPlanIfMissing(
                "ELITE",
                "Elite Plan",
                799.0,
                18.0,
                30,
                50,
                100,
                100,
                true,
                true,
                true,
                true,
                true,
                true
        );
    }

    private void createPlanIfMissing(
            String name,
            String description,
            double priceInr,
            double priceUsd,
            int maxMockInterviews,
            int maxResumeScans,
            int maxCodingProblems,
            int maxAptitudeQuestions,
            boolean includesAIHints,
            boolean includesAnalytics,
            boolean includesTier1Companies,
            boolean includesPriorityCompute,
            boolean active,
            boolean featured
    ) {
        if (!planRepository.existsByName(name)) {
            Plan plan = Plan.builder()
                    .name(name)
                    .description(description)
                    .priceInr(priceInr)
                    .priceUsd(priceUsd)
                    .interval("MONTHLY")
                    .maxMockInterviews(maxMockInterviews)
                    .maxResumeScans(maxResumeScans)
                    .maxCodingProblems(maxCodingProblems)
                    .maxAptitudeQuestions(maxAptitudeQuestions)
                    .includesAIHints(includesAIHints)
                    .includesAnalytics(includesAnalytics)
                    .includesTier1Companies(includesTier1Companies)
                    .includesPriorityCompute(includesPriorityCompute)
                    .active(active)
                    .featured(featured)
                    .createdAt(LocalDateTime.now())
                    .updatedAt(LocalDateTime.now())
                    .build();

            planRepository.save(plan);
        }
    }

    public Plan getActivePlan(String planName) {
        if (planName == null || planName.isBlank()) {
            throw new IllegalArgumentException("Plan name is required");
        }

        return planRepository.findByNameAndActiveTrue(planName.toUpperCase())
                .orElseThrow(() ->
                        new RuntimeException("Plan not found or inactive: " + planName));
    }

    public Plan getTestPlan(String planName) {
        return getActivePlan(planName);
    }

    public double getProductionPrice(String planName) {
        return getActivePlan(planName).getPriceInr();
    }

    public double getTestPrice(String planName) {
        if ("PRO".equalsIgnoreCase(planName)) {
            return 1.0;
        }

        if ("ELITE".equalsIgnoreCase(planName)) {
            return 2.0;
        }

        return 0.0;
    }

    public double getProductionPriceUsd(String planName) {
        return getActivePlan(planName).getPriceUsd();
    }

    public double getTestPriceUsd(String planName) {
        if ("PRO".equalsIgnoreCase(planName)) {
            return 1.0;
        }

        if ("ELITE".equalsIgnoreCase(planName)) {
            return 2.0;
        }

        return 0.0;
    }

    public boolean isProOrElite(String planName) {
        return "PRO".equalsIgnoreCase(planName)
                || "ELITE".equalsIgnoreCase(planName);
    }

    public boolean isStarter(String planName) {
        return "STARTER".equalsIgnoreCase(planName);
    }
}