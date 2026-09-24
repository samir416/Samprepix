package com.aiinterview.backend.controller;

import com.aiinterview.backend.dto.payment.PaymentRequest;
import com.aiinterview.backend.dto.subscription.SubscriptionResponse;
import com.aiinterview.backend.entity.Role;
import com.aiinterview.backend.entity.Subscription;
import com.aiinterview.backend.entity.User;
import com.aiinterview.backend.repository.SubscriptionRepository;
import com.aiinterview.backend.repository.UserRepository;
import com.aiinterview.backend.service.EntitlementService;
import com.aiinterview.backend.service.PaymentService;
import com.aiinterview.backend.service.SubscriptionService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/subscription")
@RequiredArgsConstructor
public class SubscriptionController {

    private final UserRepository userRepository;
    private final SubscriptionRepository subscriptionRepository;
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

        return ResponseEntity.ok(
                Map.of(
                        "subscriptions",
                        subscriptions,
                        "effectivePlan",
                        effectivePlan
                )
        );
    }

    @GetMapping({"/my/active", "/current"})
    public ResponseEntity<?> getActiveSubscription(
            Authentication authentication) {

        User user = getAuthenticatedUser(authentication);

        String effectivePlan =
                entitlementService.getEffectivePlan(user);

        SubscriptionResponse activeSubscription =
                subscriptionService.getActiveSubscription(user.getId());

        Map<String, Object> response =
                new LinkedHashMap<>();

        if (!"STARTER".equalsIgnoreCase(effectivePlan)) {

            boolean entitlement =
                    entitlementService.hasActiveEntitlement(
                            user.getId(),
                            effectivePlan
                    );

            response.put("active", true);
            response.put("plan", effectivePlan);
            response.put(
                    "source",
                    entitlement
                            ? "ENTITLEMENT"
                            : "SUBSCRIPTION"
            );
            response.put(
                    "effectivePlan",
                    effectivePlan
            );

            if (activeSubscription != null) {

                response.put(
                        "subscriptionId",
                        activeSubscription.getId()
                );

                response.put(
                        "subscriptionStatus",
                        activeSubscription.getSubscriptionStatus()
                );

                response.put(
                        "status",
                        activeSubscription.getSubscriptionStatus()
                );

                response.put(
                        "amountPaid",
                        activeSubscription.getAmountPaid()
                );

                response.put(
                        "currency",
                        activeSubscription.getCurrency()
                );

                response.put(
                        "paymentMethod",
                        activeSubscription.getPaymentMethod()
                );

                response.put(
                        "subscribedAt",
                        activeSubscription.getSubscribedAt()
                );

                response.put(
                        "expiresAt",
                        activeSubscription.getExpiresAt()
                );

                response.put(
                        "cancelledAt",
                        activeSubscription.getCancelledAt()
                );

                response.put(
                        "autoRenew",
                        activeSubscription.isAutoRenew()
                );

                response.put(
                        "createdAt",
                        activeSubscription.getCreatedAt()
                );

            } else {

                response.put(
                        "subscriptionId",
                        null
                );

                response.put(
                        "subscriptionStatus",
                        "ACTIVE"
                );

                response.put(
                        "status",
                        "ACTIVE"
                );

                response.put(
                        "amountPaid",
                        0.0
                );

                response.put(
                        "currency",
                        "INR"
                );

                response.put(
                        "paymentMethod",
                        "ENTITLEMENT"
                );

                response.put(
                        "subscribedAt",
                        null
                );

                response.put(
                        "expiresAt",
                        null
                );

                response.put(
                        "cancelledAt",
                        null
                );

                response.put(
                        "autoRenew",
                        false
                );

                response.put(
                        "createdAt",
                        null
                );
            }

            response.put(
                    "lifetime",
                    false
            );

            response.put(
                    "premium",
                    true
            );

            return ResponseEntity.ok(response);
        }

        if (activeSubscription != null) {

            response.put(
                    "active",
                    true
            );

            response.put(
                    "plan",
                    activeSubscription.getPlanName()
            );

            response.put(
                    "effectivePlan",
                    activeSubscription.getPlanName()
            );

            response.put(
                    "subscriptionId",
                    activeSubscription.getId()
            );

            response.put(
                    "subscriptionStatus",
                    activeSubscription.getSubscriptionStatus()
            );

            response.put(
                    "status",
                    activeSubscription.getSubscriptionStatus()
            );

            response.put(
                    "amountPaid",
                    activeSubscription.getAmountPaid()
            );

            response.put(
                    "currency",
                    activeSubscription.getCurrency()
            );

            response.put(
                    "paymentMethod",
                    activeSubscription.getPaymentMethod()
            );

            response.put(
                    "subscribedAt",
                    activeSubscription.getSubscribedAt()
            );

            response.put(
                    "expiresAt",
                    activeSubscription.getExpiresAt()
            );

            response.put(
                    "cancelledAt",
                    activeSubscription.getCancelledAt()
            );

            response.put(
                    "autoRenew",
                    activeSubscription.isAutoRenew()
            );

            response.put(
                    "createdAt",
                    activeSubscription.getCreatedAt()
            );

            response.put(
                    "lifetime",
                    false
            );

            response.put(
                    "premium",
                    false
            );

            response.put(
                    "source",
                    "SUBSCRIPTION"
            );

            return ResponseEntity.ok(response);
        }

        response.put(
                "active",
                false
        );

        response.put(
                "plan",
                "STARTER"
        );

        response.put(
                "effectivePlan",
                "STARTER"
        );

        response.put(
                "subscriptionId",
                null
        );

        response.put(
                "subscriptionStatus",
                "INACTIVE"
        );

        response.put(
                "status",
                "INACTIVE"
        );

        response.put(
                "amountPaid",
                0.0
        );

        response.put(
                "currency",
                "INR"
        );

        response.put(
                "paymentMethod",
                null
        );

        response.put(
                "subscribedAt",
                null
        );

        response.put(
                "expiresAt",
                null
        );

        response.put(
                "cancelledAt",
                null
        );

        response.put(
                "autoRenew",
                false
        );

        response.put(
                "createdAt",
                null
        );

        response.put(
                "lifetime",
                false
        );

        response.put(
                "premium",
                false
        );

        response.put(
                "source",
                "STARTER"
        );

        return ResponseEntity.ok(response);
    }

    @GetMapping("/admin/all")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<Subscription>> getAllSubscriptions() {

        return ResponseEntity.ok(
                subscriptionRepository.findAll()
        );
    }

    @PostMapping("/checkout")
    public ResponseEntity<?> createCheckoutSession(
            Authentication authentication,
            @Valid @RequestBody PaymentRequest request) {

        getAuthenticatedUser(authentication);

        if (request.getPlanId() == null
                || request.getPlanId().isBlank()) {

            return ResponseEntity.badRequest().body(
                    Map.of(
                            "message",
                            "Plan ID is required"
                    )
            );
        }

        return ResponseEntity.ok(
                Map.of(
                        "message",
                        "Use the Cashfree payment checkout flow to create and complete the order.",
                        "planId",
                        request.getPlanId()
                )
        );
    }

    @PostMapping("/subscribe")
    public ResponseEntity<?> subscribe(
            Authentication authentication,
            @Valid @RequestBody PaymentRequest request) {

        getAuthenticatedUser(authentication);

        return ResponseEntity.badRequest().body(
                Map.of(
                        "message",
                        "Direct subscription activation is not allowed. Complete the Cashfree payment first."
                )
        );
    }

    @PostMapping("/{subscriptionId}/auto-renew")
    public ResponseEntity<?> updateAutoRenew(
            Authentication authentication,
            @PathVariable Long subscriptionId,
            @RequestBody AutoRenewRequest request) {

        User user = getAuthenticatedUser(authentication);

        if (subscriptionId == null || subscriptionId <= 0) {
            return ResponseEntity.badRequest().body(
                    Map.of("error", "Invalid subscription ID")
            );
        }

        if (request == null) {
            return ResponseEntity.badRequest().body(
                    Map.of("error", "Auto-renew value is required")
            );
        }

        try {
            Subscription subscription =
                    subscriptionService.setAutoRenew(
                            subscriptionId,
                            user.getId(),
                            request.isAutoRenew()
                    );

            return ResponseEntity.ok(
                    Map.of(
                            "success", true,
                            "subscriptionId", subscription.getId(),
                            "autoRenew", subscription.isAutoRenew(),
                            "expiresAt", subscription.getExpiresAt()
                    )
            );
        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(
                    Map.of("error", e.getMessage())
            );
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(
                    Map.of("error", e.getMessage())
            );
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(
                    Map.of("error", e.getMessage())
            );
        }
    }

    @GetMapping("/capabilities")
    public ResponseEntity<?> getCapabilities(
            Authentication authentication) {

        User user =
                getAuthenticatedUser(authentication);

        String effectivePlan =
                entitlementService.getEffectivePlan(user);

        Map<String, Object> capabilities =
                new LinkedHashMap<>();

        capabilities.put(
                "effectivePlan",
                effectivePlan
        );

        capabilities.put(
                "maxMockInterviews",
                entitlementService
                        .getMaxMockInterviews(user)
        );

        capabilities.put(
                "maxResumeScans",
                entitlementService
                        .getMaxResumeScans(user)
        );

        capabilities.put(
                "maxCodingProblems",
                entitlementService
                        .getMaxCodingProblems(user)
        );

        capabilities.put(
                "maxAptitudeQuestions",
                entitlementService
                        .getMaxAptitudeQuestions(user)
        );

        capabilities.put(
                "includesAIHints",
                entitlementService
                        .hasAiHintsAccess(user)
        );

        capabilities.put(
                "includesAnalytics",
                entitlementService
                        .hasAnalyticsAccess(user)
        );

        capabilities.put(
                "includesTier1Companies",
                entitlementService
                        .hasTier1CompaniesAccess(user)
        );

        capabilities.put(
                "includesPriorityCompute",
                entitlementService
                        .hasPriorityComputeAccess(user)
        );

        capabilities.put(
                "hasPremiumAccess",
                entitlementService
                        .hasPremiumAccess(user)
        );

        capabilities.put(
                "hasEliteAccess",
                entitlementService
                        .hasEliteAccess(user)
        );

        capabilities.put(
                "isAdmin",
                user.getRole() == Role.ADMIN
        );

        capabilities.put(
                "lifetime",
                false
        );

        capabilities.put(
                "premiumBadge",
                !"STARTER".equalsIgnoreCase(effectivePlan)
                        ? effectivePlan
                        : null
        );

        return ResponseEntity.ok(
                capabilities
        );
    }

    @PostMapping("/admin/{subscriptionId}/cancel-refund")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> adminCancelAndRefund(
            Authentication authentication,
            @PathVariable Long subscriptionId,
            @Valid @RequestBody CancellationRequest request) {

        User admin =
                getAuthenticatedUser(authentication);

        try {

            return ResponseEntity.ok(
                    paymentService.cancelAndRefundSubscription(
                            subscriptionId,
                            admin.getId(),
                            request.getReason().trim()
                    )
            );

        } catch (SecurityException e) {

            return ResponseEntity
                    .status(HttpStatus.FORBIDDEN)
                    .body(
                            Map.of(
                                    "error",
                                    e.getMessage()
                            )
                    );

        } catch (IllegalArgumentException e) {

            return ResponseEntity
                    .badRequest()
                    .body(
                            Map.of(
                                    "error",
                                    e.getMessage()
                            )
                    );

        } catch (IllegalStateException e) {

            return ResponseEntity
                    .status(HttpStatus.CONFLICT)
                    .body(
                            Map.of(
                                    "error",
                                    e.getMessage()
                            )
                    );

        } catch (RuntimeException e) {

            return ResponseEntity
                    .status(HttpStatus.BAD_GATEWAY)
                    .body(
                            Map.of(
                                    "error",
                                    e.getMessage() != null
                                            ? e.getMessage()
                                            : "Refund processing failed"
                            )
                    );
        }
    }

    @GetMapping("/admin/refunds/{refundId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> getRefundDetails(
            Authentication authentication,
            @PathVariable Long refundId) {

        User admin =
                getAuthenticatedUser(authentication);

        try {

            return ResponseEntity.ok(
                    paymentService.getRefundDetails(
                            refundId,
                            admin.getId()
                    )
            );

        } catch (SecurityException e) {

            return ResponseEntity
                    .status(HttpStatus.FORBIDDEN)
                    .body(
                            Map.of(
                                    "error",
                                    e.getMessage()
                            )
                    );

        } catch (RuntimeException e) {

            return ResponseEntity
                    .badRequest()
                    .body(
                            Map.of(
                                    "error",
                                    e.getMessage() != null
                                            ? e.getMessage()
                                            : "Unable to fetch refund details"
                            )
                    );
        }
    }

    private User getAuthenticatedUser(
            Authentication authentication) {

        if (authentication == null
                || !authentication.isAuthenticated()
                || authentication.getName() == null
                || authentication.getName().isBlank()) {

            throw new SecurityException(
                    "Authentication required"
            );
        }

        String email = authentication.getName().trim();

        if (email.length() > 254) {
            throw new SecurityException(
                    "Invalid authentication identity"
            );
        }

        return userRepository
                .findByEmail(email)
                .orElseThrow(
                        () -> new SecurityException(
                                "User not found"
                        )
                );
    }

    @Data
    public static class AutoRenewRequest {
        private boolean autoRenew;
    }

    @Data
    public static class CancellationRequest {

        @NotBlank
        private String reason;
    }
}