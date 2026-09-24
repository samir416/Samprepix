package com.aiinterview.backend.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "manual_entitlements",
        indexes = {
                @Index(name = "idx_manual_entitlement_user", columnList = "user_id"),
                @Index(name = "idx_manual_entitlement_plan", columnList = "planName"),
                @Index(name = "idx_manual_entitlement_status", columnList = "grantStatus")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ManualEntitlement {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false, length = 20)
    private String planName;

    @Column(nullable = false, length = 30)
    private String type;

    @Column(nullable = false, length = 255)
    private String grantedBy;

    @Column(length = 1000)
    private String reason;

    private LocalDateTime grantedAt;

    private LocalDateTime expiresAt;

    @Column(nullable = false)
    private boolean revoked;

    private LocalDateTime revokedAt;

    @Column(nullable = false, length = 30)
    private String grantStatus;

    @Column(nullable = false, length = 20)
    private String emailMode;

    @Column(nullable = false)
    private boolean emailRequired;

    @Column(nullable = false)
    private boolean emailSent;

    @Column(length = 100)
    private String emailStatus;

    private LocalDateTime emailSentAt;

    @Column(length = 1000)
    private String emailFailureReason;

    @Column(length = 255)
    private String emailSubject;

    @Column(columnDefinition = "TEXT")
    private String emailBody;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    @PrePersist
    public void onCreate() {

        LocalDateTime now = LocalDateTime.now();

        if (createdAt == null) {
            createdAt = now;
        }

        if (updatedAt == null) {
            updatedAt = now;
        }

        if (grantedAt == null) {
            grantedAt = now;
        }

        if (grantStatus == null
                || grantStatus.isBlank()) {
            grantStatus = "PENDING";
        }

        if (emailMode == null
                || emailMode.isBlank()) {
            emailMode = "AUTO";
        }

        if (emailStatus == null
                || emailStatus.isBlank()) {
            emailStatus = "PENDING";
        }

        if (expiresAt == null) {
            emailRequired = true;
        }
    }

    @PreUpdate
    public void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}