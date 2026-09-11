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

    public static final String OWNER_EMAIL = "samirprajapat5@gmail.com";

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

        long activeUsers =
                userRepository.countByAccountStatus(AccountStatus.ACTIVE);

        long pendingUsers =
                userRepository.countByAccountStatus(AccountStatus.PENDING);

        List<Subscription> subscriptions =
                subscriptionRepository.findAll();

        long totalSubscriptions =
                subscriptions.size();

        long activeSubscriptions =
                subscriptions.stream()
                        .filter(subscription ->
                                subscription.getSubscriptionStatus() != null
                                && SubscriptionStatus.ACTIVE.equals(
                                        subscription.getSubscriptionStatus()))
                        .count();

        List<Payment> payments =
                paymentRepository.findAll();

        long totalPayments =
                payments.size();

        double totalRevenue =
                payments.stream()
                        .filter(payment ->
                                payment.getPaymentStatus() != null
                                && PaymentStatus.SUCCESS.name()
                                        .equalsIgnoreCase(
                                                String.valueOf(
                                                        payment.getPaymentStatus())))
                        .mapToDouble(Payment::getAmount)
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
                        .build()
        );
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
                Sort.by("createdAt").descending()
        );

        Page<User> users;

        Role roleEnum = null;
        AccountStatus accountStatusEnum = null;

        if (role != null && !role.isBlank()) {
            try {
                roleEnum = Role.valueOf(role.trim().toUpperCase());
            } catch (IllegalArgumentException ex) {
                return ResponseEntity.badRequest().build();
            }
        }

        if (accountStatus != null && !accountStatus.isBlank()) {
            try {
                accountStatusEnum =
                        AccountStatus.valueOf(
                                accountStatus.trim().toUpperCase()
                        );
            } catch (IllegalArgumentException ex) {
                return ResponseEntity.badRequest().build();
            }
        }

        if (roleEnum != null && accountStatusEnum != null) {

            users = userRepository.findByRoleAndAccountStatus(
                    roleEnum,
                    accountStatusEnum,
                    pageable
            );

        } else if (roleEnum != null) {

            users = userRepository.findByRole(
                    roleEnum,
                    pageable
            );

        } else if (accountStatusEnum != null) {

            users = userRepository.findByAccountStatus(
                    accountStatusEnum,
                    pageable
            );

        } else {

            users = userRepository.findAll(pageable);
        }

        return ResponseEntity.ok(
                users.map(this::toUserAdminResponse)
        );
    }

    @GetMapping("/users/{id}")
    public ResponseEntity<UserAdminResponse> getUserById(
            @PathVariable Long id) {

        User user = userRepository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException("User not found"));

        return ResponseEntity.ok(
                toUserAdminResponse(user)
        );
    }

    private UserAdminResponse toUserAdminResponse(User user) {

        String subscriptionStatus = "None";

        if (user.getSubscriptions() != null) {

            subscriptionStatus =
                    user.getSubscriptions()
                            .stream()
                            .filter(subscription ->
                                    subscription.getSubscriptionStatus() != null
                                    && SubscriptionStatus.ACTIVE.equals(
                                            subscription
                                                    .getSubscriptionStatus()))
                            .findFirst()
                            .map(subscription ->
                                    String.valueOf(
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
                                : null
                )
                .createdAt(user.getCreatedAt())
                .currentPlan(
                        effectiveEntitlementService
                                .getEffectivePlan(user)
                )
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
                .orElseThrow(() ->
                        new RuntimeException("User not found"));

        if (OWNER_EMAIL.equalsIgnoreCase(user.getEmail())) {
            return ResponseEntity.badRequest().body(
                    Map.of("message", "Owner account role cannot be modified")
            );
        }

        if (request.getRole() == Role.ADMIN) {
            return ResponseEntity.badRequest().body(
                    Map.of("message", "Assigning ADMIN role is prohibited. Only the single owner account has ADMIN privileges.")
            );
        }

        user.setRole(request.getRole());

        userRepository.save(user);

        return ResponseEntity.ok(
                Map.of(
                        "message",
                        "User role updated successfully"
                )
        );
    }

    // =========================================================
    // USER STATUS
    // =========================================================

    @PutMapping("/users/{id}/status")
    public ResponseEntity<?> updateUserStatus(
            @PathVariable Long id,
            @RequestParam String accountStatus) {

        User user = userRepository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException("User not found"));

        if (OWNER_EMAIL.equalsIgnoreCase(user.getEmail())) {
            return ResponseEntity.badRequest().body(
                    Map.of("message", "Owner account status cannot be modified")
            );
        }

        final AccountStatus status;

        try {

            status = AccountStatus.valueOf(
                    accountStatus.trim().toUpperCase()
            );

        } catch (IllegalArgumentException ex) {

            return ResponseEntity.badRequest()
                    .body(
                            Map.of(
                                    "message",
                                    "Invalid account status: "
                                            + accountStatus
                            )
                    );
        }

        user.setAccountStatus(status);

        userRepository.save(user);

        return ResponseEntity.ok(
                Map.of(
                        "message",
                        "User status updated successfully"
                )
        );
    }

    // =========================================================
    // DELETE USER
    // =========================================================

    @DeleteMapping("/users/{id}")
    public ResponseEntity<?> deleteUser(
            @PathVariable Long id) {

        User user = userRepository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException("User not found"));

        if (OWNER_EMAIL.equalsIgnoreCase(user.getEmail())) {
            return ResponseEntity.badRequest().body(
                    Map.of("message", "Owner account cannot be deleted")
            );
        }

        userService.deleteUser(id);

        return ResponseEntity.ok(
                Map.of(
                        "message",
                        "User deleted successfully"
                )
        );
    }

    // =========================================================
    // USER SUBSCRIPTIONS
    // =========================================================

    @GetMapping("/users/{id}/subscriptions")
    public ResponseEntity<List<Subscription>> getUserSubscriptions(
            @PathVariable Long id) {

        User user = userRepository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException("User not found"));

        return ResponseEntity.ok(
                user.getSubscriptions()
        );
    }

    // =========================================================
    // TEMPORARY ENTITLEMENT
    // =========================================================

    @PostMapping("/users/{id}/entitlements/temporary")
    public ResponseEntity<?> grantTemporaryEntitlement(
            @PathVariable Long id,
            @RequestBody Map<String, Object> request) {

        userRepository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException("User not found"));

        String planName =
                request.get("planName") != null
                        ? request.get("planName").toString()
                        : null;

        if (planName == null || planName.isBlank()) {

            return ResponseEntity.badRequest()
                    .body(
                            Map.of(
                                    "message",
                                    "Plan name is required"
                            )
                    );
        }

        int durationDays =
                request.get("durationDays") != null
                        ? ((Number) request.get("durationDays"))
                                .intValue()
                        : 30;

        if (durationDays <= 0) {

            return ResponseEntity.badRequest()
                    .body(
                            Map.of(
                                    "message",
                                    "Duration must be greater than zero"
                            )
                    );
        }

        String reason =
                request.get("reason") != null
                        ? request.get("reason").toString()
                        : null;

        ManualEntitlement entitlement =
                entitlementService.grantTemporaryAccess(
                        id,
                        planName,
                        "ADMIN",
                        durationDays,
                        reason
                );

        return ResponseEntity.ok(
                Map.of(
                        "message",
                        "Temporary entitlement granted",
                        "entitlementId",
                        entitlement.getId()
                )
        );
    }

    // =========================================================
    // LIFETIME ENTITLEMENT
    // =========================================================

    @PostMapping("/users/{id}/entitlements/lifetime")
    public ResponseEntity<?> grantLifetimeEntitlement(
            @PathVariable Long id,
            @RequestBody Map<String, Object> request) {

        userRepository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException("User not found"));

        String planName =
                request.get("planName") != null
                        ? request.get("planName").toString()
                        : null;

        if (planName == null || planName.isBlank()) {

            return ResponseEntity.badRequest()
                    .body(
                            Map.of(
                                    "message",
                                    "Plan name is required"
                            )
                    );
        }

        String reason =
                request.get("reason") != null
                        ? request.get("reason").toString()
                        : null;

        ManualEntitlement entitlement =
                entitlementService.grantLifetimeAccess(
                        id,
                        planName,
                        "ADMIN",
                        reason
                );

        return ResponseEntity.ok(
                Map.of(
                        "message",
                        "Lifetime entitlement granted",
                        "entitlementId",
                        entitlement.getId()
                )
        );
    }

    // =========================================================
    // REVOKE SINGLE ENTITLEMENT
    // =========================================================

    @DeleteMapping("/users/{id}/entitlements/{entitlementId}")
    public ResponseEntity<?> revokeEntitlement(
            @PathVariable Long id,
            @PathVariable Long entitlementId) {

        userRepository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException("User not found"));

        entitlementService.revokeEntitlement(
                entitlementId,
                "ADMIN"
        );

        return ResponseEntity.ok(
                Map.of(
                        "message",
                        "Entitlement revoked"
                )
        );
    }

    // =========================================================
    // REVOKE ALL ENTITLEMENTS
    // =========================================================

    @DeleteMapping("/users/{id}/entitlements")
    public ResponseEntity<?> revokeAllEntitlements(
            @PathVariable Long id) {

        userRepository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException("User not found"));

        entitlementService.revokeAllEntitlementsForUser(id);

        return ResponseEntity.ok(
                Map.of(
                        "message",
                        "All entitlements revoked"
                )
        );
    }

    // =========================================================
    // ACTIVE ENTITLEMENTS
    // =========================================================

    @GetMapping("/users/{id}/entitlements")
    public ResponseEntity<List<ManualEntitlement>> getUserEntitlements(
            @PathVariable Long id) {

        userRepository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException("User not found"));

        return ResponseEntity.ok(
                entitlementService.getActiveEntitlements(id)
        );
    }

    // =========================================================
    // ENTITLEMENT HISTORY
    // =========================================================

    @GetMapping("/entitlements/history/{userId}")
    public ResponseEntity<List<EntitlementHistory>> getEntitlementHistory(
            @PathVariable Long userId) {

        return ResponseEntity.ok(
                entitlementService.getEntitlementHistory(userId)
        );
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
                                "STARTER", 0.0
                        ),
                        "production",
                        Map.of(
                                "PRO", 399.0,
                                "ELITE", 799.0,
                                "STARTER", 0.0
                        )
                )
        );
    }

    // =========================================================
    // BILLING HISTORY
    // =========================================================

    @GetMapping("/billing/{userId}")
    public ResponseEntity<List<Payment>> getBillingHistory(
            @PathVariable Long userId) {

        userRepository.findById(userId)
                .orElseThrow(() ->
                        new RuntimeException("User not found"));

        return ResponseEntity.ok(
                paymentRepository.findByUserId(userId)
        );
    }
}