package com.aiinterview.backend.service;

import com.aiinterview.backend.entity.EntitlementHistory;
import com.aiinterview.backend.entity.ManualEntitlement;
import com.aiinterview.backend.entity.Subscription;
import com.aiinterview.backend.entity.User;
import com.aiinterview.backend.repository.EntitlementHistoryRepository;
import com.aiinterview.backend.repository.ManualEntitlementRepository;
import com.aiinterview.backend.repository.SubscriptionRepository;
import com.aiinterview.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

import org.springframework.context.annotation.Lazy;

@Service
public class AdminEntitlementService {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(AdminEntitlementService.class);

    private final ManualEntitlementRepository manualEntitlementRepository;
    private final EntitlementHistoryRepository entitlementHistoryRepository;
    private final SubscriptionRepository subscriptionRepository;
    private final UserRepository userRepository;
    private final PlanService planService;
    private final PaymentService paymentService;
    private final EmailService emailService;

    public AdminEntitlementService(
            ManualEntitlementRepository manualEntitlementRepository,
            EntitlementHistoryRepository entitlementHistoryRepository,
            SubscriptionRepository subscriptionRepository,
            UserRepository userRepository,
            PlanService planService,
            @Lazy PaymentService paymentService,
            EmailService emailService
    ) {
        this.manualEntitlementRepository = manualEntitlementRepository;
        this.entitlementHistoryRepository = entitlementHistoryRepository;
        this.subscriptionRepository = subscriptionRepository;
        this.userRepository = userRepository;
        this.planService = planService;
        this.paymentService = paymentService;
        this.emailService = emailService;
    }

    @Transactional
    public ManualEntitlement grantTemporaryAccess(
            Long userId,
            String planName,
            String grantedBy,
            int durationDays,
            String reason) {

        User user = getUser(userId);
        String normalizedPlan = validatePlan(planName);

        if (durationDays <= 0) {
            throw new IllegalArgumentException(
                    "Duration must be greater than 0 days"
            );
        }

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime expiresAt =
                now.plusDays(durationDays);

        ManualEntitlement entitlement =
                ManualEntitlement.builder()
                        .user(user)
                        .planName(normalizedPlan)
                        .type("TEMPORARY")
                        .grantedBy(
                                grantedBy != null
                                        ? grantedBy
                                        : "ADMIN"
                        )
                        .reason(reason)
                        .grantedAt(now)
                        .expiresAt(expiresAt)
                        .revoked(true)
                        .revokedAt(null)
                        .grantStatus("PENDING")
                        .emailMode("AUTO")
                        .emailRequired(true)
                        .emailSent(false)
                        .emailStatus("PENDING")
                        .emailSentAt(null)
                        .emailFailureReason(null)
                        .emailSubject(null)
                        .emailBody(null)
                        .createdAt(now)
                        .updatedAt(now)
                        .build();

        ManualEntitlement saved =
                manualEntitlementRepository.save(entitlement);

        EntitlementHistory history =
                EntitlementHistory.builder()
                        .user(user)
                        .planName(normalizedPlan)
                        .action("GRANT_TEMPORARY_PENDING")
                        .grantedBy(
                                grantedBy != null
                                        ? grantedBy
                                        : "ADMIN"
                        )
                        .reason(reason)
                        .effectiveAt(now)
                        .expiresAt(expiresAt)
                        .createdAt(now)
                        .build();

        entitlementHistoryRepository.save(history);

        return saved;
    }

    @Transactional
    public ManualEntitlement grantLifetimeAccess(
            Long userId,
            String planName,
            String grantedBy,
            String reason) {

        User user = getUser(userId);
        String normalizedPlan = validatePlan(planName);

        LocalDateTime now = LocalDateTime.now();

        ManualEntitlement entitlement =
                ManualEntitlement.builder()
                        .user(user)
                        .planName(normalizedPlan)
                        .type("LIFETIME")
                        .grantedBy(
                                grantedBy != null
                                        ? grantedBy
                                        : "ADMIN"
                        )
                        .reason(reason)
                        .grantedAt(now)
                        .expiresAt(null)
                        .revoked(true)
                        .revokedAt(null)
                        .grantStatus("PENDING")
                        .emailMode("AUTO")
                        .emailRequired(true)
                        .emailSent(false)
                        .emailStatus("PENDING")
                        .emailSentAt(null)
                        .emailFailureReason(null)
                        .emailSubject(null)
                        .emailBody(null)
                        .createdAt(now)
                        .updatedAt(now)
                        .build();

        ManualEntitlement saved =
                manualEntitlementRepository.save(entitlement);

        EntitlementHistory history =
                EntitlementHistory.builder()
                        .user(user)
                        .planName(normalizedPlan)
                        .action("GRANT_LIFETIME_PENDING")
                        .grantedBy(
                                grantedBy != null
                                        ? grantedBy
                                        : "ADMIN"
                        )
                        .reason(reason)
                        .effectiveAt(now)
                        .expiresAt(null)
                        .createdAt(now)
                        .build();

        entitlementHistoryRepository.save(history);

        return saved;
    }

    @Transactional
    public ManualEntitlement activateEntitlementAfterEmail(
            Long entitlementId,
            String emailMode,
            String emailSubject,
            String emailBody) {

        ManualEntitlement entitlement =
                manualEntitlementRepository.findById(entitlementId)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Entitlement not found"
                                )
                        );

        if (!"PENDING".equalsIgnoreCase(
                entitlement.getGrantStatus()
        )) {
            throw new IllegalStateException(
                    "Entitlement is not pending"
            );
        }

        entitlement.setEmailMode(
                emailMode != null && !emailMode.isBlank()
                        ? emailMode.trim().toUpperCase(Locale.ROOT)
                        : "AUTO"
        );

        entitlement.setEmailSubject(emailSubject);
        entitlement.setEmailBody(emailBody);
        entitlement.setEmailSent(true);
        entitlement.setEmailStatus("SENT");
        entitlement.setEmailSentAt(LocalDateTime.now());
        entitlement.setEmailFailureReason(null);

        entitlement.setRevoked(false);
        entitlement.setRevokedAt(null);
        entitlement.setGrantStatus("ACTIVE");
        entitlement.setUpdatedAt(LocalDateTime.now());

        ManualEntitlement saved =
                manualEntitlementRepository.save(entitlement);

        EntitlementHistory history =
                EntitlementHistory.builder()
                        .user(entitlement.getUser())
                        .planName(entitlement.getPlanName())
                        .action("GRANT_ACTIVATED")
                        .grantedBy(entitlement.getGrantedBy())
                        .reason(entitlement.getReason())
                        .effectiveAt(LocalDateTime.now())
                        .expiresAt(entitlement.getExpiresAt())
                        .createdAt(LocalDateTime.now())
                        .build();

        entitlementHistoryRepository.save(history);

        return saved;
    }

    @Transactional
    public ManualEntitlement markEmailFailed(
            Long entitlementId,
            String failureReason) {

        ManualEntitlement entitlement =
                manualEntitlementRepository.findById(entitlementId)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Entitlement not found"
                                )
                        );

        entitlement.setEmailSent(false);
        entitlement.setEmailStatus("FAILED");
        entitlement.setEmailFailureReason(
                failureReason != null
                        ? failureReason
                        : "Unable to send entitlement email"
        );
        entitlement.setGrantStatus("EMAIL_FAILED");
        entitlement.setRevoked(true);
        entitlement.setUpdatedAt(LocalDateTime.now());

        return manualEntitlementRepository.save(entitlement);
    }

    @Transactional
    public void revokeEntitlement(
            Long entitlementId,
            String revokedBy) {

        ManualEntitlement entitlement =
                manualEntitlementRepository.findById(entitlementId)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Entitlement not found"
                                )
                        );

        if (entitlement.isRevoked()
                && "REVOKED".equalsIgnoreCase(
                entitlement.getGrantStatus()
        )) {
            throw new RuntimeException(
                    "Entitlement already revoked"
            );
        }

        LocalDateTime now = LocalDateTime.now();

        entitlement.setRevoked(true);
        entitlement.setRevokedAt(now);
        entitlement.setGrantStatus("REVOKED");
        entitlement.setUpdatedAt(now);

        manualEntitlementRepository.save(entitlement);

        EntitlementHistory history =
                EntitlementHistory.builder()
                        .user(entitlement.getUser())
                        .planName(entitlement.getPlanName())
                        .action("REVOKE")
                        .grantedBy(
                                revokedBy != null
                                        ? revokedBy
                                        : "SYSTEM"
                        )
                        .reason("Entitlement revoked")
                        .effectiveAt(now)
                        .createdAt(now)
                        .build();

        entitlementHistoryRepository.save(history);
    }

    @Transactional
    public void revokeAllEntitlementsForUser(
            Long userId) {
        revokeAllEntitlementsForUser(userId, null);
    }

    @Transactional
    public void revokeAllEntitlementsForUser(
            Long userId,
            Long adminUserId) {

        User user = getUser(userId);

        List<ManualEntitlement> active =
                manualEntitlementRepository
                        .findByUserIdAndRevokedFalse(userId);

        LocalDateTime now = LocalDateTime.now();

        for (ManualEntitlement entitlement : active) {

            entitlement.setRevoked(true);
            entitlement.setRevokedAt(now);
            entitlement.setGrantStatus("REVOKED");
            entitlement.setUpdatedAt(now);

            manualEntitlementRepository.save(entitlement);

            EntitlementHistory history =
                    EntitlementHistory.builder()
                            .user(user)
                            .planName(
                                    entitlement.getPlanName()
                            )
                            .action("REVOKE_ALL")
                            .grantedBy("ADMIN")
                            .reason(
                                    "All manual entitlements revoked"
                            )
                            .effectiveAt(now)
                            .createdAt(now)
                            .build();

            entitlementHistoryRepository.save(history);
        }

        List<Subscription> allSubscriptions =
                subscriptionRepository.findByUserId(userId);

        for (Subscription subscription : allSubscriptions) {
            if ("ACTIVE".equalsIgnoreCase(subscription.getSubscriptionStatus()) || subscription.isLifetime()) {
                try {
                    if (subscription.getCashfreeOrderId() != null && !subscription.getCashfreeOrderId().isBlank()) {
                        paymentService.cancelAndRefundSubscriptionByAdmin(subscription.getId(), adminUserId, "Revoked by admin");
                    } else {
                        subscription.setSubscriptionStatus("CANCELLED");
                        subscription.setCancelledAt(now);
                        subscription.setCancelledBy("ADMIN");
                        subscription.setCancellationReason("Revoked by admin");
                        subscription.setAutoRenew(false);
                        subscription.setLifetime(false);
                        if (subscription.getExpiresAt() == null || subscription.getExpiresAt().isAfter(now)) {
                            subscription.setExpiresAt(now);
                        }
                        subscription.setUpdatedAt(now);
                        subscriptionRepository.save(subscription);

                        if (user.getEmail() != null && !user.getEmail().isBlank()) {
                            String customerName = user.getName() != null && !user.getName().isBlank()
                                    ? user.getName()
                                    : (user.getUsername() != null ? user.getUsername() : "Valued Member");
                            emailService.sendSubscriptionRevokedEmail(
                                    user.getEmail(),
                                    customerName,
                                    subscription.getPlan() != null ? subscription.getPlan().getName() : "Premium",
                                    0.0,
                                    "N/A",
                                    "Platform access updated"
                            );
                        }
                    }
                } catch (Exception ex) {
                    log.error("Failed to cancel/refund subscription id {} for user {}: {}", subscription.getId(), userId, ex.getMessage());
                }

                String planName = subscription.getPlan() != null
                        ? subscription.getPlan().getName()
                        : "UNKNOWN";

                EntitlementHistory history =
                        EntitlementHistory.builder()
                                .user(user)
                                .planName(planName)
                                .action("REVOKE_SUBSCRIPTION")
                                .grantedBy("ADMIN")
                                .reason("Subscription revoked by admin")
                                .effectiveAt(now)
                                .createdAt(now)
                                .build();

                entitlementHistoryRepository.save(history);
            }
        }
    }

    @Transactional(readOnly = true)
    public List<EntitlementHistory> getEntitlementHistory(
            Long userId) {

        getUser(userId);

        return entitlementHistoryRepository
                .findByUserId(userId);
    }

    @Transactional(readOnly = true)
    public List<ManualEntitlement> getActiveEntitlements(
            Long userId) {

        getUser(userId);

        LocalDateTime now = LocalDateTime.now();

        return manualEntitlementRepository
                .findByUserIdAndRevokedFalse(userId)
                .stream()
                .filter(entitlement ->
                        "ACTIVE".equalsIgnoreCase(
                                entitlement.getGrantStatus()
                        )
                                && !entitlement.isRevoked()
                                && (
                                entitlement.getExpiresAt() == null
                                        || entitlement.getExpiresAt()
                                        .isAfter(now)
                        )
                )
                .toList();
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

        String normalizedPlan =
                planName.trim().toUpperCase(Locale.ROOT);

        LocalDateTime now = LocalDateTime.now();

        return manualEntitlementRepository
                .findByUserIdAndRevokedFalse(userId)
                .stream()
                .anyMatch(entitlement ->
                        normalizedPlan.equals(
                                entitlement.getPlanName()
                        )
                                && "ACTIVE".equalsIgnoreCase(
                                entitlement.getGrantStatus()
                        )
                                && !entitlement.isRevoked()
                                && (
                                entitlement.getExpiresAt() == null
                                        || entitlement.getExpiresAt()
                                        .isAfter(now)
                        )
                );
    }

    @Transactional(readOnly = true)
    public Optional<ManualEntitlement> getActiveEntitlement(
            Long userId,
            String planName) {

        if (userId == null
                || planName == null
                || planName.isBlank()) {
            return Optional.empty();
        }

        String normalizedPlan =
                planName.trim().toUpperCase(Locale.ROOT);

        LocalDateTime now = LocalDateTime.now();

        return manualEntitlementRepository
                .findByUserIdAndRevokedFalse(userId)
                .stream()
                .filter(entitlement ->
                        normalizedPlan.equals(
                                entitlement.getPlanName()
                        )
                )
                .filter(entitlement ->
                        "ACTIVE".equalsIgnoreCase(
                                entitlement.getGrantStatus()
                        )
                )
                .filter(entitlement ->
                        !entitlement.isRevoked()
                )
                .filter(entitlement ->
                        entitlement.getExpiresAt() == null
                                || entitlement.getExpiresAt()
                                .isAfter(now)
                )
                .findFirst();
    }

    private User getUser(Long userId) {

        if (userId == null) {
            throw new IllegalArgumentException(
                    "User ID is required"
            );
        }

        return userRepository.findById(userId)
                .orElseThrow(() ->
                        new RuntimeException(
                                "User not found"
                        )
                );
    }

    private String validatePlan(String planName) {

        if (planName == null
                || planName.isBlank()) {

            throw new IllegalArgumentException(
                    "Plan name is required"
            );
        }

        String normalized =
                planName.trim().toUpperCase(Locale.ROOT);

        if (!"PRO".equals(normalized)
                && !"ELITE".equals(normalized)) {

            throw new IllegalArgumentException(
                    "Only PRO and ELITE entitlements can be granted"
            );
        }

        planService.getActivePlan(normalized);

        return normalized;
    }
}