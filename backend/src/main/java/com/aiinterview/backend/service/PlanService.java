package com.aiinterview.backend.service;

import com.aiinterview.backend.entity.Plan;
import com.aiinterview.backend.repository.PlanRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Locale;

@Service
@RequiredArgsConstructor
public class PlanService {

    private static final String MONTH_INTERVAL = "MONTH";
    private static final String STARTER = "STARTER";
    private static final String PRO = "PRO";
    private static final String ELITE = "ELITE";

    private final PlanRepository planRepository;

    @EventListener(ApplicationReadyEvent.class)
    @Transactional
    public void seedPlans() {
        createPlanIfMissing(
                STARTER,
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
                PRO,
                "Pro Plan",
                399.0,
                9.0,
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
                ELITE,
                "Elite Plan",
                799.0,
                19.0,
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
        String normalizedName = normalizePlanName(name);

        if (!planRepository.existsByNameIgnoreCase(normalizedName)) {
            LocalDateTime now = LocalDateTime.now();

            Plan plan = Plan.builder()
                    .name(normalizedName)
                    .description(description)
                    .priceInr(priceInr)
                    .priceUsd(priceUsd)
                    .interval(MONTH_INTERVAL)
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
                    .createdAt(now)
                    .updatedAt(now)
                    .build();

            planRepository.save(plan);
        }
    }

    @Transactional(readOnly = true)
    public Plan getActivePlan(String planName) {
        String normalizedName = normalizePlanName(planName);

        return planRepository.findByNameIgnoreCaseAndActiveTrue(normalizedName)
                .orElseThrow(() ->
                        new IllegalArgumentException("Plan not found or inactive"));
    }

    @Transactional(readOnly = true)
    public Plan getTestPlan(String planName) {
        return getActivePlan(planName);
    }

    @Transactional(readOnly = true)
    public double getProductionPrice(String planName) {
        return getActivePlan(planName).getPriceInr();
    }

    @Transactional(readOnly = true)
    public double getTestPrice(String planName) {
        String normalizedName = normalizePlanName(planName);

        if (PRO.equals(normalizedName)) {
            return 1.0;
        }

        if (ELITE.equals(normalizedName)) {
            return 2.0;
        }

        return 0.0;
    }

    @Transactional(readOnly = true)
    public double getProductionPriceUsd(String planName) {
        return getActivePlan(planName).getPriceUsd();
    }

    @Transactional(readOnly = true)
    public double getTestPriceUsd(String planName) {
        String normalizedName = normalizePlanName(planName);

        if (PRO.equals(normalizedName)) {
            return 1.0;
        }

        if (ELITE.equals(normalizedName)) {
            return 2.0;
        }

        return 0.0;
    }

    public boolean isProOrElite(String planName) {
        if (planName == null || planName.isBlank()) {
            return false;
        }

        String normalizedName = planName.trim().toUpperCase(Locale.ROOT);

        return PRO.equals(normalizedName)
                || ELITE.equals(normalizedName);
    }

    public boolean isStarter(String planName) {
        if (planName == null || planName.isBlank()) {
            return false;
        }

        return STARTER.equals(planName.trim().toUpperCase(Locale.ROOT));
    }

    private String normalizePlanName(String planName) {
        if (planName == null || planName.isBlank()) {
            throw new IllegalArgumentException("Plan name is required");
        }

        String normalizedName = planName.trim().toUpperCase(Locale.ROOT);

        if (!normalizedName.matches("[A-Z0-9_-]{1,30}")) {
            throw new IllegalArgumentException("Invalid plan name");
        }

        return normalizedName;
    }
}
