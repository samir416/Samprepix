package com.aiinterview.backend.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "upgrade_transactions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpgradeTransaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "subscription_id", nullable = false)
    private Subscription subscription;

    @Column(nullable = false, length = 30)
    private String previousPlan;

    @Column(nullable = false, length = 30)
    private String targetPlan;

    @Column(nullable = false)
    private double originalPaidAmount;

    @Column(nullable = false)
    private double targetPlanPrice;

    @Column(nullable = false)
    private double upgradeAmount;

    @Column(nullable = false)
    private LocalDateTime subscriptionExpiry;

    @Column(nullable = false, unique = true, length = 100)
    private String cashfreeOrderId;

    @Column(nullable = true, length = 100)
    private String cashfreePaymentId;

    @Column(nullable = false, length = 30)
    private String paymentStatus;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    private LocalDateTime completedAt;

    @PrePersist
    public void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
        if (paymentStatus == null || paymentStatus.isBlank()) {
            paymentStatus = "PENDING";
        }
    }
}
