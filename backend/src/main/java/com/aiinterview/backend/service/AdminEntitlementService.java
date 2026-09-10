package com.aiinterview.backend.service;

import com.aiinterview.backend.entity.*;
import com.aiinterview.backend.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AdminEntitlementService {

    private final ManualEntitlementRepository manualEntitlementRepository;
    private final EntitlementHistoryRepository entitlementHistoryRepository;
    private final UserRepository userRepository;
    private final PlanService planService;

    @Transactional
    public ManualEntitlement grantTemporaryAccess(Long userId, String planName, String grantedBy,
                                                  int durationDays, String reason) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        ManualEntitlement entitlement = ManualEntitlement.builder()
                .user(user)
                .planName(planName)
                .type("TEMPORARY")
                .grantedBy(grantedBy)
                .reason(reason)
                .grantedAt(LocalDateTime.now())
                .expiresAt(LocalDateTime.now().plusDays(durationDays))
                .revoked(false)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        ManualEntitlement saved = manualEntitlementRepository.save(entitlement);

        EntitlementHistory history = EntitlementHistory.builder()
                .user(user)
                .planName(planName)
                .action("GRANT_TEMPORARY")
                .grantedBy(grantedBy)
                .reason(reason)
                .effectiveAt(LocalDateTime.now())
                .expiresAt(LocalDateTime.now().plusDays(durationDays))
                .createdAt(LocalDateTime.now())
                .build();
        entitlementHistoryRepository.save(history);

        return saved;
    }

    @Transactional
    public ManualEntitlement grantLifetimeAccess(Long userId, String planName, String grantedBy, String reason) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        ManualEntitlement entitlement = ManualEntitlement.builder()
                .user(user)
                .planName(planName)
                .type("LIFETIME")
                .grantedBy(grantedBy)
                .reason(reason)
                .grantedAt(LocalDateTime.now())
                .expiresAt(null)
                .revoked(false)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        ManualEntitlement saved = manualEntitlementRepository.save(entitlement);

        EntitlementHistory history = EntitlementHistory.builder()
                .user(user)
                .planName(planName)
                .action("GRANT_LIFETIME")
                .grantedBy(grantedBy)
                .reason(reason)
                .effectiveAt(LocalDateTime.now())
                .createdAt(LocalDateTime.now())
                .build();
        entitlementHistoryRepository.save(history);

        return saved;
    }

    @Transactional
    public void revokeEntitlement(Long entitlementId, String revokedBy) {
        ManualEntitlement entitlement = manualEntitlementRepository.findById(entitlementId)
                .orElseThrow(() -> new RuntimeException("Entitlement not found"));

        if (entitlement.isRevoked()) {
            throw new RuntimeException("Entitlement already revoked");
        }

        entitlement.setRevoked(true);
        entitlement.setRevokedAt(LocalDateTime.now());
        entitlement.setUpdatedAt(LocalDateTime.now());
        manualEntitlementRepository.save(entitlement);

        EntitlementHistory history = EntitlementHistory.builder()
                .user(entitlement.getUser())
                .planName(entitlement.getPlanName())
                .action("REVOKE")
                .grantedBy(revokedBy)
                .effectiveAt(LocalDateTime.now())
                .createdAt(LocalDateTime.now())
                .build();
        entitlementHistoryRepository.save(history);
    }

    @Transactional
    public void revokeAllEntitlementsForUser(Long userId) {
        List<ManualEntitlement> active = manualEntitlementRepository.findByUserIdAndRevokedFalse(userId);
        for (ManualEntitlement e : active) {
            e.setRevoked(true);
            e.setRevokedAt(LocalDateTime.now());
            e.setUpdatedAt(LocalDateTime.now());
            manualEntitlementRepository.save(e);

            EntitlementHistory history = EntitlementHistory.builder()
                    .user(e.getUser())
                    .planName(e.getPlanName())
                    .action("REVOKE_ALL")
                    .grantedBy("SYSTEM")
                    .effectiveAt(LocalDateTime.now())
                    .createdAt(LocalDateTime.now())
                    .build();
            entitlementHistoryRepository.save(history);
        }
    }

    public List<EntitlementHistory> getEntitlementHistory(Long userId) {
        return entitlementHistoryRepository.findByUserId(userId);
    }

    public List<ManualEntitlement> getActiveEntitlements(Long userId) {
        return manualEntitlementRepository.findByUserIdAndRevokedFalse(userId);
    }

    public boolean hasActiveEntitlement(Long userId, String planName) {
        List<ManualEntitlement> entitlements = manualEntitlementRepository.findByUserIdAndRevokedFalse(userId);
        return entitlements.stream().anyMatch(e -> {
            if (!e.getPlanName().equals(planName)) return false;
            if (e.isRevoked()) return false;
            if (e.getExpiresAt() != null && e.getExpiresAt().isBefore(LocalDateTime.now())) return false;
            return true;
        });
    }

    public Optional<ManualEntitlement> getActiveEntitlement(Long userId, String planName) {
        return manualEntitlementRepository.findByUserIdAndRevokedFalse(userId).stream()
                .filter(e -> e.getPlanName().equals(planName))
                .filter(e -> !e.isRevoked())
                .filter(e -> e.getExpiresAt() == null || e.getExpiresAt().isAfter(LocalDateTime.now()))
                .findFirst();
    }
}
