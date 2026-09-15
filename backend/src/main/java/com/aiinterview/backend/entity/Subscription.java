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

    @Column(nullable = false)
    private String subscriptionStatus;

    /*
     * Razorpay subscription ID is optional because the current
     * one-time payment flow creates a Razorpay Order + Payment,
     * not a recurring Razorpay Subscription.
     */
    @Column(nullable = true)
    private String razorpaySubscriptionId;

    @Column(nullable = true)
    private String razorpayOrderId;

    @Column(nullable = true)
    private String cashfreeOrderId;

    @Column(nullable = true)
    private String razorpayPaymentId;

    @Column(nullable = false)
    private double amountPaid;

    @Column(nullable = false)
    private String currency;

    @Column
    private String paymentMethod;

    private LocalDateTime subscribedAt;

    private LocalDateTime expiresAt;

    private LocalDateTime cancelledAt;

    @Column(nullable = false)
    private boolean autoRenew;

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
    }

    @PreUpdate
    public void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}