package com.aiinterview.backend.service;

import com.aiinterview.backend.dto.subscription.SubscriptionResponse;
import com.aiinterview.backend.entity.Plan;
import com.aiinterview.backend.entity.Subscription;
import com.aiinterview.backend.entity.User;
import com.aiinterview.backend.dto.subscription.UpgradeQuoteResponse;
import com.aiinterview.backend.repository.PlanRepository;
import com.aiinterview.backend.repository.SubscriptionRepository;
import com.aiinterview.backend.repository.UpgradeTransactionRepository;
import com.aiinterview.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SubscriptionService {

    private final SubscriptionRepository subscriptionRepository;
    private final UserRepository userRepository;
    private final PlanRepository planRepository;
    private final UpgradeTransactionRepository upgradeTransactionRepository;

    @Transactional(readOnly = true)
    public List<SubscriptionResponse> getUserSubscriptions(Long userId) {
        getUser(userId);

        return subscriptionRepository
                .findByUserIdOrderBySubscribedAtDesc(userId)
                .stream()
                .map(subscription ->
                        SubscriptionResponse.fromEntity(
                                subscription,
                                subscription.getPlan() != null
                                        ? subscription.getPlan().getName()
                                        : "UNKNOWN"
                        )
                )
                .toList();
    }

    @Transactional(readOnly = true)
    public SubscriptionResponse getActiveSubscription(Long userId) {
        User user = getUser(userId);

        Subscription subscription = getLatestActiveSubscription(user);

        if (subscription == null) {
            return null;
        }

        return SubscriptionResponse.fromEntity(
                subscription,
                subscription.getPlan() != null
                        ? subscription.getPlan().getName()
                        : "UNKNOWN"
        );
    }

    @Transactional(readOnly = true)
    public boolean hasActiveSubscription(Long userId) {
        return getLatestActiveSubscription(getUser(userId)) != null;
    }

    @Transactional(readOnly = true)
    public Plan getActivePlan(Long userId) {
        Subscription subscription =
                getLatestActiveSubscription(getUser(userId));

        return subscription != null
                ? subscription.getPlan()
                : null;
    }

    @Transactional(readOnly = true)
    public Subscription getActiveSubscriptionEntity(Long userId) {
        return getLatestActiveSubscription(getUser(userId));
    }

    @Transactional(readOnly = true)
    public boolean hasLifetimeSubscription(Long userId) {
        return subscriptionRepository
                .findByUserIdAndSubscriptionStatusOrderBySubscribedAtDesc(
                        userId,
                        "ACTIVE"
                )
                .stream()
                .anyMatch(subscription ->
                        subscription.isLifetime()
                                && isSubscriptionActive(subscription)
                );
    }

    @Transactional(readOnly = true)
    public boolean hasPremiumSubscription(Long userId) {
        return subscriptionRepository
                .findByUserIdAndSubscriptionStatusOrderBySubscribedAtDesc(
                        userId,
                        "ACTIVE"
                )
                .stream()
                .filter(this::isSubscriptionActive)
                .map(Subscription::getPlan)
                .anyMatch(this::isPremiumPlan);
    }

    @Transactional(readOnly = true)
    public boolean hasEliteSubscription(Long userId) {
        return subscriptionRepository
                .findByUserIdAndSubscriptionStatusOrderBySubscribedAtDesc(
                        userId,
                        "ACTIVE"
                )
                .stream()
                .filter(this::isSubscriptionActive)
                .map(Subscription::getPlan)
                .anyMatch(plan ->
                        plan != null
                                && "ELITE".equalsIgnoreCase(plan.getName())
                );
    }

    @Transactional
    public Subscription markSubscriptionCancelled(
            Long subscriptionId,
            Long adminUserId,
            String reason
    ) {
        if (subscriptionId == null) {
            throw new IllegalArgumentException("Subscription ID is required");
        }

        if (adminUserId == null) {
            throw new IllegalArgumentException("Admin user ID is required");
        }

        if (reason == null || reason.isBlank()) {
            throw new IllegalArgumentException("Cancellation reason is required");
        }

        User admin = getUser(adminUserId);

        if (admin.getRole() == null
                || !"ADMIN".equalsIgnoreCase(admin.getRole().name())) {
            throw new SecurityException(
                    "Only administrators can cancel subscriptions"
            );
        }

        Subscription subscription = subscriptionRepository
                .findById(subscriptionId)
                .orElseThrow(() ->
                        new RuntimeException("Subscription not found")
                );

        if (!"ACTIVE".equalsIgnoreCase(
                subscription.getSubscriptionStatus()
        )) {
            throw new IllegalStateException(
                    "Only an active subscription can be cancelled"
            );
        }

        subscription.setSubscriptionStatus("CANCELLED");
        subscription.setCancelledAt(LocalDateTime.now());
        subscription.setCancelledBy(admin.getEmail());
        subscription.setCancellationReason(reason.trim());
        subscription.setAutoRenew(false);

        LocalDateTime now = LocalDateTime.now();

        if (subscription.getExpiresAt() == null
                || subscription.getExpiresAt().isAfter(now)) {
            subscription.setExpiresAt(now);
        }

        return subscriptionRepository.save(subscription);
    }

    @Transactional
    public Subscription setAutoRenew(
            Long subscriptionId,
            Long userId,
            boolean autoRenew
    ) {
        if (subscriptionId == null) {
            throw new IllegalArgumentException("Subscription ID is required");
        }

        if (userId == null) {
            throw new IllegalArgumentException("User ID is required");
        }

        User user = getUser(userId);

        Subscription subscription = subscriptionRepository
                .findByIdAndUserId(subscriptionId, user.getId())
                .orElseThrow(() ->
                        new RuntimeException("Subscription not found")
                );

        if (!"ACTIVE".equalsIgnoreCase(
                subscription.getSubscriptionStatus()
        )) {
            throw new IllegalStateException(
                    "Only an active subscription can change auto-renew"
            );
        }

        if (!isSubscriptionActive(subscription)) {
            throw new IllegalStateException(
                    "Subscription has expired"
            );
        }

        subscription.setAutoRenew(autoRenew);
        if (autoRenew) {
            subscription.setNextRenewalDate(subscription.getExpiresAt());
        } else {
            subscription.setNextRenewalDate(null);
        }

        return subscriptionRepository.save(subscription);
    }

    @Transactional(readOnly = true)
    public UpgradeQuoteResponse getUpgradeQuote(Long userId) {
        User user = getUser(userId);
        Subscription activeSub = getLatestActiveSubscription(user);

        if (activeSub == null) {
            return UpgradeQuoteResponse.builder()
                    .eligible(false)
                    .reason("No active subscription found. Upgrade requires an active Pro subscription.")
                    .build();
        }

        if (activeSub.getPlan() == null) {
            return UpgradeQuoteResponse.builder()
                    .eligible(false)
                    .reason("Active subscription has no plan associated.")
                    .build();
        }

        String currentPlanName = activeSub.getPlan().getName().toUpperCase();

        if ("ELITE".equalsIgnoreCase(currentPlanName)) {
            return UpgradeQuoteResponse.builder()
                    .eligible(false)
                    .reason("You are already on the highest tier (Elite).")
                    .currentPlan("ELITE")
                    .targetPlan("ELITE")
                    .activeSubscriptionId(activeSub.getId())
                    .originalExpiresAt(activeSub.getExpiresAt())
                    .build();
        }

        if (!"PRO".equalsIgnoreCase(currentPlanName)) {
            return UpgradeQuoteResponse.builder()
                    .eligible(false)
                    .reason("Upgrade is only available from Pro to Elite.")
                    .currentPlan(currentPlanName)
                    .build();
        }

        LocalDateTime now = LocalDateTime.now();
        if (activeSub.getExpiresAt() != null && !activeSub.getExpiresAt().isAfter(now)) {
            return UpgradeQuoteResponse.builder()
                    .eligible(false)
                    .reason("Your Pro subscription has expired. Please renew your plan.")
                    .currentPlan("PRO")
                    .build();
        }

        Plan elitePlan = planRepository.findByNameIgnoreCaseAndActiveTrue("ELITE")
                .orElse(null);

        if (elitePlan == null) {
            elitePlan = planRepository.findByNameIgnoreCase("ELITE").orElse(null);
        }

        if (elitePlan == null) {
            return UpgradeQuoteResponse.builder()
                    .eligible(false)
                    .reason("Elite plan is currently unavailable for upgrade.")
                    .currentPlan("PRO")
                    .build();
        }

        double targetPrice = elitePlan.getPriceInr();
        double currentPaid = activeSub.getAmountPaid();
        double upgradeAmount = Math.max(0.0, targetPrice - currentPaid);
        long remainingDays = activeSub.getExpiresAt() != null
                ? Math.max(0, ChronoUnit.DAYS.between(now, activeSub.getExpiresAt()))
                : 0;

        String formattedAmount = "₹" + ((int) upgradeAmount);
        String msg = "Upgrade to Elite — pay only " + formattedAmount + " to unlock Elite features for the remaining billing period.";

        return UpgradeQuoteResponse.builder()
                .eligible(true)
                .currentPlan("PRO")
                .targetPlan("ELITE")
                .currentPaidAmount(currentPaid)
                .targetPlanPrice(targetPrice)
                .upgradeAmount(upgradeAmount)
                .currency(activeSub.getCurrency() != null ? activeSub.getCurrency() : "INR")
                .originalExpiresAt(activeSub.getExpiresAt())
                .remainingDays(remainingDays)
                .notificationMessage(msg)
                .activeSubscriptionId(activeSub.getId())
                .build();
    }

    @Transactional
    public int expireSubscriptions() {
        LocalDateTime now = LocalDateTime.now();

        List<Subscription> subscriptions =
                subscriptionRepository
                        .findBySubscriptionStatusOrderBySubscribedAtDesc(
                                "ACTIVE"
                        );

        int expiredCount = 0;

        for (Subscription subscription : subscriptions) {
            if (subscription.getExpiresAt() != null
                    && !subscription.getExpiresAt().isAfter(now)) {

                subscription.setSubscriptionStatus("EXPIRED");
                subscription.setAutoRenew(false);

                subscriptionRepository.save(subscription);

                expiredCount++;
            }
        }

        return expiredCount;
    }

    private Subscription getLatestActiveSubscription(User user) {
        return subscriptionRepository
                .findByUserAndSubscriptionStatusOrderBySubscribedAtDesc(
                        user,
                        "ACTIVE"
                )
                .stream()
                .filter(this::isSubscriptionActive)
                .findFirst()
                .orElse(null);
    }

    private boolean isSubscriptionActive(Subscription subscription) {
        if (subscription == null
                || !"ACTIVE".equalsIgnoreCase(
                subscription.getSubscriptionStatus())) {
            return false;
        }

        if (subscription.getExpiresAt() == null) {
            return false;
        }

        return subscription.getExpiresAt()
                .isAfter(LocalDateTime.now());
    }

    private boolean isPremiumPlan(Plan plan) {
        return plan != null
                && (
                "PRO".equalsIgnoreCase(plan.getName())
                        || "ELITE".equalsIgnoreCase(plan.getName())
        );
    }

    private User getUser(Long userId) {
        if (userId == null) {
            throw new IllegalArgumentException("User ID is required");
        }

        return userRepository
                .findById(userId)
                .orElseThrow(() ->
                        new RuntimeException("User not found")
                );
    }
}