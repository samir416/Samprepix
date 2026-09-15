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

    @Column(nullable = true, unique = true)
    private String cashfreeOrderId;

    @Column(nullable = true)
    private String cashfreeSessionId;

    @Column(nullable = true)
    private String cashfreePaymentId;

    @Column(nullable = false)
    private double amount;

    @Column(nullable = false)
    private String currency;

    @Column(nullable = false)
    private String paymentMethod;

    @Column(nullable = false)
    private String paymentStatus;

    @Column
    private String refundId;

    private LocalDateTime paidAt;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    @PrePersist
    public void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    public void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
