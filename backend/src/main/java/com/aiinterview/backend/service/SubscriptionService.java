package com.aiinterview.backend.service;

import com.aiinterview.backend.dto.subscription.SubscriptionResponse;
import com.aiinterview.backend.entity.Plan;
import com.aiinterview.backend.entity.Subscription;
import com.aiinterview.backend.entity.User;
import com.aiinterview.backend.repository.SubscriptionRepository;
import com.aiinterview.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SubscriptionService {

    private final SubscriptionRepository subscriptionRepository;
    private final UserRepository userRepository;

    /**
     * Get all subscriptions belonging to a user.
     */
    @Transactional(readOnly = true)
    public List<SubscriptionResponse> getUserSubscriptions(Long userId) {
        User user = getUser(userId);

        return subscriptionRepository.findByUserId(userId)
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

    /**
     * Get the user's active subscription.
     */
    @Transactional(readOnly = true)
    public SubscriptionResponse getActiveSubscription(Long userId) {
        User user = getUser(userId);

        Subscription subscription = subscriptionRepository
                .findByUserAndSubscriptionStatus(user, "ACTIVE")
                .orElse(null);

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

    /**
     * Cancel a subscription.
     * Ownership is checked before cancellation.
     */
    @Transactional
    public SubscriptionResponse cancelSubscription(
            Long userId,
            Long subscriptionId
    ) {
        User user = getUser(userId);

        Subscription subscription = subscriptionRepository
                .findById(subscriptionId)
                .orElseThrow(() ->
                        new RuntimeException("Subscription not found"));

        if (subscription.getUser() == null
                || !subscription.getUser().getId().equals(user.getId())) {
            throw new RuntimeException(
                    "You are not authorized to cancel this subscription"
            );
        }

        if (!"ACTIVE".equalsIgnoreCase(
                subscription.getSubscriptionStatus())) {
            throw new RuntimeException(
                    "Only an active subscription can be cancelled"
            );
        }

        subscription.setSubscriptionStatus("CANCELLED");
        subscription.setCancelledAt(LocalDateTime.now());
        subscription.setAutoRenew(false);

        Subscription saved = subscriptionRepository.save(subscription);

        return SubscriptionResponse.fromEntity(
                saved,
                saved.getPlan() != null
                        ? saved.getPlan().getName()
                        : "UNKNOWN"
        );
    }

    /**
     * Check whether a user currently has an active subscription.
     */
    @Transactional(readOnly = true)
    public boolean hasActiveSubscription(Long userId) {
        User user = getUser(userId);

        return subscriptionRepository
                .findByUserAndSubscriptionStatus(user, "ACTIVE")
                .isPresent();
    }

    /**
     * Return the active plan for a user, if available.
     */
    @Transactional(readOnly = true)
    public Plan getActivePlan(Long userId) {
        User user = getUser(userId);

        return subscriptionRepository
                .findByUserAndSubscriptionStatus(user, "ACTIVE")
                .map(Subscription::getPlan)
                .orElse(null);
    }

    /**
     * Load user or fail with a clear error.
     */
    private User getUser(Long userId) {
        if (userId == null) {
            throw new IllegalArgumentException("User ID is required");
        }

        return userRepository.findById(userId)
                .orElseThrow(() ->
                        new RuntimeException("User not found"));
    }
}