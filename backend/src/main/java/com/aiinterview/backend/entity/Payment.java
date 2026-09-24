package com.aiinterview.backend.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "payments")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "plan_id")
    private Plan plan;

    @Column(nullable = true)
    private String razorpayOrderId;

    @Column(nullable = true)
    private String razorpayPaymentId;

    @Column(nullable = true)
    private String razorpaySignature;

    @Column(nullable = true, unique = true, length = 100)
    private String cashfreeOrderId;

    @Column(nullable = true, length = 255)
    private String cashfreeSessionId;

    @Column(nullable = true, length = 100)
    private String cashfreePaymentId;

    @Column(nullable = false)
    private double amount;

    @Column(nullable = false)
    private double displayAmount;

    @Column(nullable = false, length = 10)
    private String currency;

    @Column(nullable = false, length = 50)
    private String paymentMethod;

    @Column(nullable = false, length = 30)
    private String paymentStatus;

    @Column(nullable = true, length = 100)
    private String refundId;

    @Column(nullable = true, length = 30)
    private String refundStatus;

    @Column(nullable = true)
    private double refundAmount;

    @Column(nullable = true, length = 1000)
    private String refundReason;

    @Column(nullable = true)
    private LocalDateTime refundInitiatedAt;

    @Column(nullable = true)
    private LocalDateTime refundedAt;

    @Column(nullable = true, length = 100)
    private String cancelledBy;

    @Column(nullable = true, length = 1000)
    private String cancellationReason;

    @Column(nullable = false)
    private boolean testMode = false;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    private LocalDateTime paidAt;

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

        if (currency == null || currency.isBlank()) {
            currency = "INR";
        }

        if (paymentMethod == null || paymentMethod.isBlank()) {
            paymentMethod = "cashfree";
        }

        if (paymentStatus == null || paymentStatus.isBlank()) {
            paymentStatus = "CREATED";
        }
    }

    @PreUpdate
    public void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}