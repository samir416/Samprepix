package com.aiinterview.backend.controller;

import com.aiinterview.backend.dto.payment.PaymentRequest;
import com.aiinterview.backend.dto.subscription.SubscriptionResponse;
import com.aiinterview.backend.entity.Subscription;
import com.aiinterview.backend.entity.User;
import com.aiinterview.backend.service.EntitlementService;
import com.aiinterview.backend.service.PaymentService;
import com.aiinterview.backend.service.SubscriptionService;
import com.aiinterview.backend.repository.UserRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/subscription")
@RequiredArgsConstructor
public class SubscriptionController {

    private final UserRepository userRepository;
    private final EntitlementService entitlementService;
    private final PaymentService paymentService;
    private final SubscriptionService subscriptionService;

    @GetMapping("/my")
    public ResponseEntity<?> getMySubscription(
            Authentication authentication) {

        User user = getAuthenticatedUser(authentication);

        List<SubscriptionResponse> subscriptions =
                subscriptionService.getUserSubscriptions(user.getId());

        String effectivePlan =
                entitlementService.getEffectivePlan(user);

        return ResponseEntity.ok(Map.of(
                "subscriptions", subscriptions,
                "effectivePlan", effectivePlan
        ));
    }

    @GetMapping("/my/active")
    public ResponseEntity<?> getActiveSubscription(
            Authentication authentication) {

        User user = getAuthenticatedUser(authentication);

        String effectivePlan =
                entitlementService.getEffectivePlan(user);

        if (!"STARTER".equalsIgnoreCase(effectivePlan)) {

            boolean entitlement =
                    entitlementService.hasActiveEntitlement(
                            user.getId(),
                            effectivePlan
                    );

            Map<String, Object> response = new LinkedHashMap<>();

            response.put("active", true);
            response.put("plan", effectivePlan);
            response.put(
                    "source",
                    entitlement ? "ENTITLEMENT" : "SUBSCRIPTION"
            );

            return ResponseEntity.ok(response);
        }

        SubscriptionResponse activeSubscription =
                subscriptionService.getActiveSubscription(user.getId());

        if (activeSubscription != null) {

            Map<String, Object> response = new LinkedHashMap<>();

            response.put("active", true);
            response.put("plan", activeSubscription.getPlanName());
            response.put(
                    "subscriptionId",
                    activeSubscription.getId()
            );
            response.put("source", "SUBSCRIPTION");

            return ResponseEntity.ok(response);
        }

        return ResponseEntity.ok(Map.of(
                "active", false,
                "plan", "STARTER"
        ));
    }

    /**
     * Legacy checkout endpoint.
     *
     * Actual payment creation is handled by PaymentController
     * through Razorpay's server-side order creation flow.
     */
    @PostMapping("/checkout")
    public ResponseEntity<?> createCheckoutSession(
            Authentication authentication,
            @Valid @RequestBody PaymentRequest request) {

        getAuthenticatedUser(authentication);

        if (request.getPlanId() == null
                || request.getPlanId().isBlank()) {

            return ResponseEntity.badRequest().body(
                    Map.of("message", "Plan ID is required")
            );
        }

        return ResponseEntity.ok(Map.of(
                "message",
                "Use the payment checkout flow to create and complete the order.",
                "planId",
                request.getPlanId()
        ));
    }

    /**
     * Subscription activation must happen only after
     * successful server-side payment verification.
     *
     * Frontend cannot directly activate a subscription.
     */
    @PostMapping("/subscribe")
    public ResponseEntity<?> subscribe(
            Authentication authentication,
            @Valid @RequestBody PaymentRequest request) {

        getAuthenticatedUser(authentication);

        return ResponseEntity.badRequest().body(
                Map.of(
                        "message",
                        "Direct subscription activation is not allowed. Complete the Razorpay payment first."
                )
        );
    }

    @PostMapping("/cancel/{subscriptionId}")
    public ResponseEntity<?> cancelSubscription(
            @PathVariable Long subscriptionId,
            Authentication authentication) {

        User user = getAuthenticatedUser(authentication);

        try {
            SubscriptionResponse response =
                    subscriptionService.cancelSubscription(
                            user.getId(),
                            subscriptionId
                    );

            return ResponseEntity.ok(
                    Map.of(
                            "message",
                            "Subscription cancelled successfully",
                            "subscription",
                            response
                    )
            );

        } catch (RuntimeException ex) {

            if ("Subscription not found".equals(ex.getMessage())) {
                return ResponseEntity.notFound().build();
            }

            if (ex.getMessage() != null
                    && ex.getMessage().contains("not authorized")) {

                return ResponseEntity.status(403).body(
                        Map.of("message", ex.getMessage())
                );
            }

            return ResponseEntity.badRequest().body(
                    Map.of(
                            "message",
                            ex.getMessage() != null
                                    ? ex.getMessage()
                                    : "Unable to cancel subscription"
                    )
            );
        }
    }

    @GetMapping("/capabilities")
    public ResponseEntity<?> getCapabilities(
            Authentication authentication) {

        User user = getAuthenticatedUser(authentication);

        String effectivePlan =
                entitlementService.getEffectivePlan(user);

        Map<String, Object> capabilities =
                new LinkedHashMap<>();

        capabilities.put("effectivePlan", effectivePlan);

        capabilities.put(
                "maxMockInterviews",
                entitlementService.getMaxMockInterviews(user)
        );

        capabilities.put(
                "maxResumeScans",
                entitlementService.getMaxResumeScans(user)
        );

        capabilities.put(
                "maxCodingProblems",
                entitlementService.getMaxCodingProblems(user)
        );

        capabilities.put(
                "maxAptitudeQuestions",
                entitlementService.getMaxAptitudeQuestions(user)
        );

        capabilities.put(
                "includesAIHints",
                entitlementService.hasAiHintsAccess(user)
        );

        capabilities.put(
                "includesAnalytics",
                entitlementService.hasAnalyticsAccess(user)
        );

        capabilities.put(
                "includesTier1Companies",
                entitlementService.hasTier1CompaniesAccess(user)
        );

        capabilities.put(
                "includesPriorityCompute",
                entitlementService.hasPriorityComputeAccess(user)
        );

        capabilities.put(
                "hasPremiumAccess",
                entitlementService.hasPremiumAccess(user)
        );

        capabilities.put(
                "hasEliteAccess",
                entitlementService.hasEliteAccess(user)
        );

        capabilities.put(
                "isAdmin",
                user.getRole() ==
                        com.aiinterview.backend.entity.Role.ADMIN
        );

        return ResponseEntity.ok(capabilities);
    }

    private User getAuthenticatedUser(
            Authentication authentication) {

        if (authentication == null
                || authentication.getName() == null
                || authentication.getName().isBlank()) {

            throw new RuntimeException("Authentication required");
        }

        return userRepository.findByEmail(authentication.getName())
                .orElseThrow(
                        () -> new RuntimeException("User not found")
                );
    }
}