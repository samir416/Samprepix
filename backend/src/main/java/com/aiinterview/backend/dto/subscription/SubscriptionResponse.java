package com.aiinterview.backend.dto.subscription;

import com.aiinterview.backend.entity.Subscription;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SubscriptionResponse {

    private Long id;
    private String planName;
    private String subscriptionStatus;
    private double amountPaid;
    private String currency;
    private String paymentMethod;
    private boolean lifetime;
    private LocalDateTime subscribedAt;
    private LocalDateTime expiresAt;
    private LocalDateTime cancelledAt;
    private String cancelledBy;
    private String cancellationReason;
    private boolean refundEligible;
    private boolean refundInitiated;
    private String refundId;
    private String refundStatus;
    private LocalDateTime refundInitiatedAt;
    private LocalDateTime refundedAt;
    private String refundReason;
    private boolean autoRenew;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static SubscriptionResponse fromEntity(
            Subscription sub,
            String planName
    ) {
        if (sub == null) {
            throw new IllegalArgumentException("Subscription is required");
        }

        return SubscriptionResponse.builder()
                .id(sub.getId())
                .planName(planName)
                .subscriptionStatus(sub.getSubscriptionStatus())
                .amountPaid(sub.getAmountPaid())
                .currency(sub.getCurrency())
                .paymentMethod(sub.getPaymentMethod())
                .lifetime(false)
                .subscribedAt(sub.getSubscribedAt())
                .expiresAt(sub.getExpiresAt())
                .cancelledAt(sub.getCancelledAt())
                .cancelledBy(sub.getCancelledBy())
                .cancellationReason(sub.getCancellationReason())
                .refundEligible(sub.isRefundEligible())
                .refundInitiated(sub.isRefundInitiated())
                .refundId(sub.getRefundId())
                .refundStatus(sub.getRefundStatus())
                .refundInitiatedAt(sub.getRefundInitiatedAt())
                .refundedAt(sub.getRefundedAt())
                .refundReason(sub.getRefundReason())
                .autoRenew(sub.isAutoRenew())
                .createdAt(sub.getCreatedAt())
                .updatedAt(sub.getUpdatedAt())
                .build();
    }
}