package com.aiinterview.backend.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "refunds",
        indexes = {
                @Index(name = "idx_refund_user", columnList = "user_id"),
                @Index(name = "idx_refund_payment", columnList = "payment_id"),
                @Index(name = "idx_refund_order", columnList = "cashfreeOrderId"),
                @Index(name = "idx_refund_status", columnList = "refundStatus")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Refund {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "payment_id", nullable = false)
    private Payment payment;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "subscription_id")
    private Subscription subscription;

    @Column(nullable = false, unique = true, length = 100)
    private String refundId;

    @Column(nullable = false, length = 100)
    private String cashfreeOrderId;

    @Column(nullable = true, length = 100)
    private String cashfreePaymentId;

    @Column(nullable = false)
    private double refundAmount;

    @Column(nullable = false, length = 10)
    private String currency;

    @Column(nullable = false, length = 30)
    private String refundStatus;

    @Column(nullable = false, length = 30)
    private String refundSpeed;

    @Column(nullable = false, length = 255)
    private String initiatedBy;

    @Column(nullable = false, length = 1000)
    private String reason;

    @Column(nullable = true, length = 1000)
    private String refundNote;

    @Column(nullable = true, length = 1000)
    private String failureReason;

    @Column(nullable = false)
    private LocalDateTime initiatedAt;

    private LocalDateTime processedAt;

    private LocalDateTime completedAt;

    private LocalDateTime failedAt;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    @PrePersist
    public void onCreate() {
        LocalDateTime now = LocalDateTime.now();

        if (createdAt == null) {
            createdAt = now;
        }

        if (initiatedAt == null) {
            initiatedAt = now;
        }

        if (updatedAt == null) {
            updatedAt = now;
        }
    }

    @PreUpdate
    public void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}