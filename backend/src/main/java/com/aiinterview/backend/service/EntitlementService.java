package com.aiinterview.backend.service;

import com.aiinterview.backend.entity.ManualEntitlement;
import com.aiinterview.backend.entity.Plan;
import com.aiinterview.backend.entity.Role;
import com.aiinterview.backend.entity.Subscription;
import com.aiinterview.backend.entity.User;
import com.aiinterview.backend.repository.ManualEntitlementRepository;
import com.aiinterview.backend.repository.SubscriptionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class EntitlementService {

    private final ManualEntitlementRepository manualEntitlementRepository;
    private final SubscriptionRepository subscriptionRepository;
    private final PlanService planService;

    @Transactional(readOnly = true)
    public String getEffectivePlan(User user) {

        if (user == null) {
            return "STARTER";
        }

        if (user.getRole() == Role.ADMIN) {
            return "ADMIN";
        }

        LocalDateTime now = LocalDateTime.now();

        Optional<ManualEntitlement> activeEntitlement =
                manualEntitlementRepository
                        .findByUserIdAndRevokedFalse(user.getId())
                        .stream()
                        .filter(entitlement ->
                                "ACTIVE".equalsIgnoreCase(
                                        entitlement.getGrantStatus()
                                )
                                        && entitlement.isEmailSent()
                                        && "SENT".equalsIgnoreCase(
                                        entitlement.getEmailStatus()
                                )
                                        && !entitlement.isRevoked()
                                        && (
                                        entitlement.getExpiresAt() == null
                                                || entitlement.getExpiresAt().isAfter(now)
                                )
                        )
                        .findFirst();

        if (activeEntitlement.isPresent()) {
            return normalizePlan(
                    activeEntitlement.get().getPlanName()
            );
        }

        List<Subscription> activeSubscriptions =
                subscriptionRepository
                        .findByUserAndSubscriptionStatusOrderBySubscribedAtDesc(
                                user,
                                "ACTIVE"
                        );

        for (Subscription subscription : activeSubscriptions) {

            if (subscription.getPlan() != null
                    && subscription.getExpiresAt() != null
                    && subscription.getExpiresAt().isAfter(now)) {

                return normalizePlan(
                        subscription.getPlan().getName()
                );
            }
        }

        return "STARTER";
    }

    @Transactional(readOnly = true)
    public boolean hasActiveEntitlement(
            Long userId,
            String planName) {

        if (userId == null
                || planName == null
                || planName.isBlank()) {
            return false;
        }

        LocalDateTime now = LocalDateTime.now();

        return manualEntitlementRepository
                .findByUserIdAndRevokedFalse(userId)
                .stream()
                .anyMatch(entitlement ->
                        "ACTIVE".equalsIgnoreCase(
                                entitlement.getGrantStatus()
                        )
                                && entitlement.isEmailSent()
                                && "SENT".equalsIgnoreCase(
                                entitlement.getEmailStatus()
                        )
                                && planName.equalsIgnoreCase(
                                entitlement.getPlanName()
                        )
                                && !entitlement.isRevoked()
                                && (
                                entitlement.getExpiresAt() == null
                                        || entitlement.getExpiresAt().isAfter(now)
                        )
                );
    }

    @Transactional(readOnly = true)
    public boolean hasPremiumAccess(User user) {

        String effectivePlan = getEffectivePlan(user);

        return "PRO".equals(effectivePlan)
                || "ELITE".equals(effectivePlan)
                || "ADMIN".equals(effectivePlan);
    }

    @Transactional(readOnly = true)
    public boolean hasEliteAccess(User user) {

        String effectivePlan = getEffectivePlan(user);

        return "ELITE".equals(effectivePlan)
                || "ADMIN".equals(effectivePlan);
    }

    @Transactional(readOnly = true)
    public boolean hasAiHintsAccess(User user) {

        String effectivePlan = getEffectivePlan(user);

        if ("ADMIN".equals(effectivePlan)) {
            return true;
        }

        if ("STARTER".equals(effectivePlan)) {
            return false;
        }

        return getPlan(effectivePlan).isIncludesAIHints();
    }

    @Transactional(readOnly = true)
    public boolean hasAnalyticsAccess(User user) {

        String effectivePlan = getEffectivePlan(user);

        if ("ADMIN".equals(effectivePlan)) {
            return true;
        }

        if ("STARTER".equals(effectivePlan)) {
            return false;
        }

        return getPlan(effectivePlan).isIncludesAnalytics();
    }

    @Transactional(readOnly = true)
    public boolean hasTier1CompaniesAccess(User user) {

        String effectivePlan = getEffectivePlan(user);

        if ("ADMIN".equals(effectivePlan)) {
            return true;
        }

        if ("STARTER".equals(effectivePlan)) {
            return false;
        }

        return getPlan(effectivePlan).isIncludesTier1Companies();
    }

    @Transactional(readOnly = true)
    public boolean hasPriorityComputeAccess(User user) {

        String effectivePlan = getEffectivePlan(user);

        if ("ADMIN".equals(effectivePlan)) {
            return true;
        }

        if ("STARTER".equals(effectivePlan)) {
            return false;
        }

        return getPlan(effectivePlan).isIncludesPriorityCompute();
    }

    @Transactional(readOnly = true)
    public boolean hasGithubAnalyzerAccess(User user) {
        return hasPremiumAccess(user);
    }

    @Transactional(readOnly = true)
    public boolean hasAiRoadmapAccess(User user) {
        return hasPremiumAccess(user);
    }

    @Transactional(readOnly = true)
    public int getMaxMockInterviews(User user) {

        String effectivePlan = getEffectivePlan(user);

        if ("ADMIN".equals(effectivePlan)) {
            return Integer.MAX_VALUE;
        }

        return getPlan(effectivePlan).getMaxMockInterviews();
    }

    @Transactional(readOnly = true)
    public int getMaxResumeScans(User user) {

        String effectivePlan = getEffectivePlan(user);

        if ("ADMIN".equals(effectivePlan)) {
            return Integer.MAX_VALUE;
        }

        return getPlan(effectivePlan).getMaxResumeScans();
    }

    @Transactional(readOnly = true)
    public int getMaxCodingProblems(User user) {

        String effectivePlan = getEffectivePlan(user);

        if ("ADMIN".equals(effectivePlan)) {
            return Integer.MAX_VALUE;
        }

        return getPlan(effectivePlan).getMaxCodingProblems();
    }

    @Transactional(readOnly = true)
    public int getMaxAptitudeQuestions(User user) {

        String effectivePlan = getEffectivePlan(user);

        if ("ADMIN".equals(effectivePlan)) {
            return Integer.MAX_VALUE;
        }

        return getPlan(effectivePlan).getMaxAptitudeQuestions();
    }

    @Transactional(readOnly = true)
    public boolean hasLifetimePremiumAccess(User user) {

        if (user == null || user.getId() == null) {
            return false;
        }

        if (user.getRole() == Role.ADMIN) {
            return true;
        }

        LocalDateTime now = LocalDateTime.now();

        return manualEntitlementRepository
                .findByUserIdAndRevokedFalse(user.getId())
                .stream()
                .anyMatch(entitlement ->
                        "ACTIVE".equalsIgnoreCase(
                                entitlement.getGrantStatus()
                        )
                                && entitlement.isEmailSent()
                                && "SENT".equalsIgnoreCase(
                                entitlement.getEmailStatus()
                        )
                                && !entitlement.isRevoked()
                                && entitlement.getExpiresAt() == null
                                && (
                                "PRO".equalsIgnoreCase(
                                        entitlement.getPlanName()
                                )
                                        || "ELITE".equalsIgnoreCase(
                                        entitlement.getPlanName()
                                )
                        )
                );
    }

    @Transactional(readOnly = true)
    public boolean hasManualPremiumAccess(User user) {

        if (user == null || user.getId() == null) {
            return false;
        }

        LocalDateTime now = LocalDateTime.now();

        return manualEntitlementRepository
                .findByUserIdAndRevokedFalse(user.getId())
                .stream()
                .anyMatch(entitlement ->
                        "ACTIVE".equalsIgnoreCase(
                                entitlement.getGrantStatus()
                        )
                                && entitlement.isEmailSent()
                                && "SENT".equalsIgnoreCase(
                                entitlement.getEmailStatus()
                        )
                                && !entitlement.isRevoked()
                                && (
                                "PRO".equalsIgnoreCase(
                                        entitlement.getPlanName()
                                )
                                        || "ELITE".equalsIgnoreCase(
                                        entitlement.getPlanName()
                                )
                        )
                                && (
                                entitlement.getExpiresAt() == null
                                        || entitlement.getExpiresAt().isAfter(now)
                        )
                );
    }

    @Transactional(readOnly = true)
    public boolean hasProAccess(User user) {

        String effectivePlan = getEffectivePlan(user);

        return "PRO".equals(effectivePlan)
                || "ELITE".equals(effectivePlan)
                || "ADMIN".equals(effectivePlan);
    }

    @Transactional(readOnly = true)
    public boolean hasAnyPremiumPlan(User user) {

        String effectivePlan = getEffectivePlan(user);

        return "PRO".equals(effectivePlan)
                || "ELITE".equals(effectivePlan);
    }

    private Plan getPlan(String planName) {

        if (planName == null || planName.isBlank()) {
            return planService.getActivePlan("STARTER");
        }

        try {
            return planService.getActivePlan(planName);
        } catch (RuntimeException ex) {
            return planService.getActivePlan("STARTER");
        }
    }

    private String normalizePlan(String planName) {

        if (planName == null || planName.isBlank()) {
            return "STARTER";
        }

        String normalized =
                planName.trim().toUpperCase();

        if ("ADMIN".equals(normalized)
                || "ELITE".equals(normalized)
                || "PRO".equals(normalized)
                || "STARTER".equals(normalized)) {

            return normalized;
        }

        return "STARTER";
    }
}