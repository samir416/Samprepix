package com.aiinterview.backend.service;

import com.aiinterview.backend.entity.*;
import com.aiinterview.backend.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class EntitlementService {

    private final ManualEntitlementRepository manualEntitlementRepository;
    private final SubscriptionRepository subscriptionRepository;
    private final PlanService planService;

    @Transactional
    public String getEffectivePlan(User user) {
        if (user.getRole() == Role.ADMIN) {
            return "ADMIN";
        }

        Optional<ManualEntitlement> activeEntitlement = manualEntitlementRepository
                .findByUserIdAndRevokedFalse(user.getId()).stream()
                .filter(e -> e.getExpiresAt() == null
                        || e.getExpiresAt().isAfter(LocalDateTime.now()))
                .filter(e -> !e.isRevoked())
                .findFirst();

        if (activeEntitlement.isPresent()) {
            return activeEntitlement.get().getPlanName();
        }

        Optional<Subscription> activeSub =
                subscriptionRepository.findByUserAndSubscriptionStatus(
                        user, "ACTIVE");

        if (activeSub.isPresent()) {
            Subscription sub = activeSub.get();

            if (sub.getExpiresAt() != null
                    && sub.getExpiresAt().isAfter(LocalDateTime.now())) {
                return sub.getPlan().getName();
            }
        }

        return "STARTER";
    }

    /**
     * Checks whether the user has an active manual entitlement
     * for the requested plan.
     */
    public boolean hasActiveEntitlement(Long userId, String planName) {
        if (userId == null || planName == null || planName.isBlank()) {
            return false;
        }

        LocalDateTime now = LocalDateTime.now();

        return manualEntitlementRepository
                .findByUserIdAndRevokedFalse(userId)
                .stream()
                .anyMatch(entitlement ->
                        planName.equalsIgnoreCase(entitlement.getPlanName())
                                && !entitlement.isRevoked()
                                && (entitlement.getExpiresAt() == null
                                || entitlement.getExpiresAt().isAfter(now))
                );
    }

    public boolean hasPremiumAccess(User user) {
        String effectivePlan = getEffectivePlan(user);

        return "PRO".equals(effectivePlan)
                || "ELITE".equals(effectivePlan)
                || "ADMIN".equals(effectivePlan);
    }

    public boolean hasEliteAccess(User user) {
        String effectivePlan = getEffectivePlan(user);

        return "ELITE".equals(effectivePlan)
                || "ADMIN".equals(effectivePlan);
    }

    public boolean hasAiHintsAccess(User user) {
        String effectivePlan = getEffectivePlan(user);
        return !"STARTER".equals(effectivePlan);
    }

    public boolean hasAnalyticsAccess(User user) {
        String effectivePlan = getEffectivePlan(user);
        return !"STARTER".equals(effectivePlan);
    }

    public boolean hasTier1CompaniesAccess(User user) {
        String effectivePlan = getEffectivePlan(user);
        return !"STARTER".equals(effectivePlan);
    }

    public boolean hasPriorityComputeAccess(User user) {
        String effectivePlan = getEffectivePlan(user);
        return !"STARTER".equals(effectivePlan);
    }

    public int getMaxMockInterviews(User user) {
        String effectivePlan = getEffectivePlan(user);

        return switch (effectivePlan) {
            case "PRO" -> 15;
            case "ELITE" -> 30;
            default -> 3;
        };
    }

    public int getMaxResumeScans(User user) {
        String effectivePlan = getEffectivePlan(user);

        return switch (effectivePlan) {
            case "PRO" -> 20;
            case "ELITE" -> 50;
            default -> 5;
        };
    }

    public int getMaxCodingProblems(User user) {
        String effectivePlan = getEffectivePlan(user);

        return switch (effectivePlan) {
            case "PRO" -> 50;
            case "ELITE" -> 100;
            default -> 10;
        };
    }

    public int getMaxAptitudeQuestions(User user) {
        String effectivePlan = getEffectivePlan(user);

        return switch (effectivePlan) {
            case "PRO" -> 50;
            case "ELITE" -> 100;
            default -> 20;
        };
    }
}