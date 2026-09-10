package com.aiinterview.backend.controller;

import com.aiinterview.backend.dto.admin.*;
import com.aiinterview.backend.entity.*;
import com.aiinterview.backend.repository.PaymentRepository;
import com.aiinterview.backend.repository.SubscriptionRepository;
import com.aiinterview.backend.repository.UserRepository;
import com.aiinterview.backend.service.*;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    private final UserRepository userRepository;
    private final UserService userService;
    private final AdminEntitlementService entitlementService;
    private final PlanService planService;
    private final PaymentRepository paymentRepository;
    private final SubscriptionRepository subscriptionRepository;
    private final EntitlementService effectiveEntitlementService;

    // =========================================================
    // ADMIN DASHBOARD STATS
    // =========================================================

    @GetMapping("/stats")
    public ResponseEntity<AdminStatsResponse> getStats() {

        long totalUsers = userRepository.count();

        long activeUsers = userRepository.countByAccountStatus("ACTIVE");

        long pendingUsers = userRepository.countByAccountStatus("PENDING");

        List<Subscription> subscriptions = subscriptionRepository.findAll();

        long totalSubscriptions = subscriptions.size();

        long activeSubscriptions = subscriptions.stream()
                .filter(subscription -> subscription.getSubscriptionStatus() != null
                        && "ACTIVE".equalsIgnoreCase(
                                String.valueOf(
                                        subscription.getSubscriptionStatus())))
                .count();

        List<Payment> payments = paymentRepository.findAll();

        long totalPayments = payments.size();

        double totalRevenue = payments.stream()
                .filter(payment -> payment.getPaymentStatus() != null
                        && "SUCCESS".equalsIgnoreCase(
                                String.valueOf(
                                        payment.getPaymentStatus())))
                .mapToDouble(payment -> {
                    return payment.getAmount();
                })
                .sum();

        return ResponseEntity.ok(
                AdminStatsResponse.builder()
                        .totalUsers(totalUsers)
                        .activeUsers(activeUsers)
                        .pendingUsers(pendingUsers)
                        .totalSubscriptions(totalSubscriptions)
                        .activeSubscriptions(activeSubscriptions)
                        .totalPayments(totalPayments)
                        .totalRevenue(totalRevenue)
                        .periodStart(null)
                        .periodEnd(null)
                        .build());
    }

    // =========================================================
    // USER MANAGEMENT
    // =========================================================

    @GetMapping("/users")
    public ResponseEntity<Page<UserAdminResponse>> getAllUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String role,
            @RequestParam(required = false) String accountStatus) {

        int safePage = Math.max(0, page);
        int safeSize = Math.min(100, Math.max(1, size));

        PageRequest pageable = PageRequest.of(
                safePage,
                safeSize,
                Sort.by("createdAt").descending());

        Page<User> users;

        if (role != null
                && !role.isBlank()
                && accountStatus != null
                && !accountStatus.isBlank()) {

            users = userRepository.findByRoleAndAccountStatus(
                    Role.valueOf(role.toUpperCase()),
                    accountStatus,
                    pageable);

        } else if (role != null && !role.isBlank()) {

            users = userRepository.findByRole(
                    Role.valueOf(role.toUpperCase()),
                    pageable);

        } else if (accountStatus != null
                && !accountStatus.isBlank()) {

            users = userRepository.findByAccountStatus(
                    accountStatus,
                    pageable);

        } else {

            users = userRepository.findAll(pageable);
        }

        return ResponseEntity.ok(
                users.map(this::toUserAdminResponse));
    }

    @GetMapping("/users/{id}")
    public ResponseEntity<UserAdminResponse> getUserById(
            @PathVariable Long id) {

        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));

        return ResponseEntity.ok(
                toUserAdminResponse(user));
    }

    private UserAdminResponse toUserAdminResponse(User user) {

        String subscriptionStatus = "None";

        if (user.getSubscriptions() != null) {

            subscriptionStatus = user.getSubscriptions()
                    .stream()
                    .filter(subscription -> subscription.getSubscriptionStatus() != null
                            && "ACTIVE".equalsIgnoreCase(
                                    String.valueOf(
                                            subscription
                                                    .getSubscriptionStatus())))
                    .findFirst()
                    .map(subscription -> String.valueOf(
                            subscription
                                    .getSubscriptionStatus()))
                    .orElse("None");
        }

        return UserAdminResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .name(user.getName())
                .role(user.getRole())
                .accountStatus(user.getAccountStatus())
                .emailVerified(user.isEmailVerified())
                .provider(
                        user.getProvider() != null
                                ? user.getProvider().name()
                                : null)
                .createdAt(user.getCreatedAt())
                .currentPlan(
                        effectiveEntitlementService
                                .getEffectivePlan(user))
                .subscriptionStatus(subscriptionStatus)
                .build();
    }

    // =========================================================
    // USER ROLE
    // =========================================================

    @PutMapping("/users/{id}/role")
    public ResponseEntity<?> updateUserRole(
            @PathVariable Long id,
            @Valid @RequestBody UpdateUserRoleRequest request) {

        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));

        user.setRole(request.getRole());

        userRepository.save(user);

        return ResponseEntity.ok(
                Map.of(
                        "message",
                        "User role updated successfully"));
    }

    // =========================================================
    // USER STATUS
    // =========================================================

    @PutMapping("/users/{id}/status")
    public ResponseEntity<?> updateUserStatus(
            @PathVariable Long id,
            @RequestParam String accountStatus) {

        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));

        user.setAccountStatus(
                AccountStatus.valueOf(
                        accountStatus.toUpperCase()));

        userRepository.save(user);

        return ResponseEntity.ok(
                Map.of(
                        "message",
                        "User status updated successfully"));
    }

    // =========================================================
    // DELETE USER
    // =========================================================

    @DeleteMapping("/users/{id}")
    public ResponseEntity<?> deleteUser(
            @PathVariable Long id) {

        userService.deleteUser(id);

        return ResponseEntity.ok(
                Map.of(
                        "message",
                        "User deleted successfully"));
    }

    // =========================================================
    // USER SUBSCRIPTIONS
    // =========================================================

    @GetMapping("/users/{id}/subscriptions")
    public ResponseEntity<List<Subscription>> getUserSubscriptions(
            @PathVariable Long id) {

        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));

        return ResponseEntity.ok(
                user.getSubscriptions());
    }

    // =========================================================
    // TEMPORARY ENTITLEMENT
    // =========================================================

    @PostMapping("/users/{id}/entitlements/temporary")
    public ResponseEntity<?> grantTemporaryEntitlement(
            @PathVariable Long id,
            @RequestBody Map<String, Object> request) {

        userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));

        String planName = request.get("planName") != null
                ? request.get("planName").toString()
                : null;

        if (planName == null || planName.isBlank()) {
            return ResponseEntity.badRequest()
                    .body(Map.of(
                            "message",
                            "Plan name is required"));
        }

        int durationDays = request.get("durationDays") != null
                ? ((Number) request.get("durationDays"))
                        .intValue()
                : 30;

        if (durationDays <= 0) {
            return ResponseEntity.badRequest()
                    .body(Map.of(
                            "message",
                            "Duration must be greater than zero"));
        }

        String reason = request.get("reason") != null
                ? request.get("reason").toString()
                : null;

        ManualEntitlement entitlement = entitlementService.grantTemporaryAccess(
                id,
                planName,
                "ADMIN",
                durationDays,
                reason);

        return ResponseEntity.ok(
                Map.of(
                        "message",
                        "Temporary entitlement granted",
                        "entitlementId",
                        entitlement.getId()));
    }

    // =========================================================
    // LIFETIME ENTITLEMENT
    // =========================================================

    @PostMapping("/users/{id}/entitlements/lifetime")
    public ResponseEntity<?> grantLifetimeEntitlement(
            @PathVariable Long id,
            @RequestBody Map<String, Object> request) {

        userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));

        String planName = request.get("planName") != null
                ? request.get("planName").toString()
                : null;

        if (planName == null || planName.isBlank()) {
            return ResponseEntity.badRequest()
                    .body(Map.of(
                            "message",
                            "Plan name is required"));
        }

        String reason = request.get("reason") != null
                ? request.get("reason").toString()
                : null;

        ManualEntitlement entitlement = entitlementService.grantLifetimeAccess(
                id,
                planName,
                "ADMIN",
                reason);

        return ResponseEntity.ok(
                Map.of(
                        "message",
                        "Lifetime entitlement granted",
                        "entitlementId",
                        entitlement.getId()));
    }

    // =========================================================
    // REVOKE SINGLE ENTITLEMENT
    // =========================================================

    @DeleteMapping("/users/{id}/entitlements/{entitlementId}")
    public ResponseEntity<?> revokeEntitlement(
            @PathVariable Long id,
            @PathVariable Long entitlementId) {

        userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));

        entitlementService.revokeEntitlement(
                entitlementId,
                "ADMIN");

        return ResponseEntity.ok(
                Map.of(
                        "message",
                        "Entitlement revoked"));
    }

    // =========================================================
    // REVOKE ALL ENTITLEMENTS
    // =========================================================

    @DeleteMapping("/users/{id}/entitlements")
    public ResponseEntity<?> revokeAllEntitlements(
            @PathVariable Long id) {

        userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));

        entitlementService.revokeAllEntitlementsForUser(id);

        return ResponseEntity.ok(
                Map.of(
                        "message",
                        "All entitlements revoked"));
    }

    // =========================================================
    // ACTIVE ENTITLEMENTS
    // =========================================================

    @GetMapping("/users/{id}/entitlements")
    public ResponseEntity<List<ManualEntitlement>> getUserEntitlements(
            @PathVariable Long id) {

        userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));

        return ResponseEntity.ok(
                entitlementService.getActiveEntitlements(id));
    }

    // =========================================================
    // ENTITLEMENT HISTORY
    // =========================================================

    @GetMapping("/entitlements/history/{userId}")
    public ResponseEntity<List<EntitlementHistory>> getEntitlementHistory(
            @PathVariable Long userId) {

        return ResponseEntity.ok(
                entitlementService.getEntitlementHistory(userId));
    }

    // =========================================================
    // PRICING
    // =========================================================

    @GetMapping("/plans/pricing")
    public ResponseEntity<Map<String, Object>> getPricing() {

        return ResponseEntity.ok(
                Map.of(
                        "testMode",
                        Map.of(
                                "PRO", 1.0,
                                "ELITE", 2.0,
                                "STARTER", 0.0),
                        "production",
                        Map.of(
                                "PRO", 399.0,
                                "ELITE", 799.0,
                                "STARTER", 0.0)));
    }

    // =========================================================
    // BILLING HISTORY
    // =========================================================

    @GetMapping("/billing/{userId}")
    public ResponseEntity<List<Payment>> getBillingHistory(
            @PathVariable Long userId) {

        userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        return ResponseEntity.ok(
                paymentRepository.findByUserId(userId));
    }
}