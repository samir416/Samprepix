package com.aiinterview.backend.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "subscriptions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Subscription {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "plan_id", nullable = false)
    private Plan plan;

    @Column(nullable = false, length = 30)
    private String subscriptionStatus;

    @Column(nullable = true)
    private String razorpaySubscriptionId;

    @Column(nullable = true)
    private String razorpayOrderId;

    @Column(nullable = true)
    private String razorpayPaymentId;

    @Column(nullable = true, unique = true)
    private String cashfreeOrderId;

    @Column(nullable = true)
    private String cashfreeSessionId;

    @Column(nullable = true)
    private String cashfreePaymentId;

    @Column(nullable = false)
    private double amountPaid;

    @Column(nullable = false, length = 10)
    private String currency;

    @Column(nullable = true, length = 50)
    private String paymentMethod;

    @Column(nullable = false)
    private boolean lifetime = false;

    private LocalDateTime subscribedAt;

    @Column(nullable = false)
    private LocalDateTime expiresAt;

    private LocalDateTime cancelledAt;

    @Column(nullable = true, length = 255)
    private String cancelledBy;

    @Column(nullable = true, length = 1000)
    private String cancellationReason;

    @Column(nullable = false)
    private boolean refundEligible = false;

    @Column(nullable = false)
    private boolean refundInitiated = false;

    @Column(nullable = true, length = 100)
    private String refundId;

    @Column(nullable = true, length = 30)
    private String refundStatus;

    private LocalDateTime refundInitiatedAt;

    private LocalDateTime refundedAt;

    @Column(nullable = true, length = 1000)
    private String refundReason;

    @Column(nullable = false)
    private boolean autoRenew = true;

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

        if (subscribedAt == null) {
            subscribedAt = now;
        }

        lifetime = false;

        if (expiresAt == null) {
            expiresAt = subscribedAt.plusMonths(1);
        }

        if (!autoRenew) {
            autoRenew = false;
        }

        if (!refundEligible) {
            refundEligible = true;
        }
    }

    @PreUpdate
    public void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}