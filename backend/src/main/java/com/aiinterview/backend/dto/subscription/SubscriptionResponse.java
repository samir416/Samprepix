package com.aiinterview.backend.dto.subscription;

import com.aiinterview.backend.entity.SubscriptionStatus;
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
    private LocalDateTime subscribedAt;
    private LocalDateTime expiresAt;
    private LocalDateTime cancelledAt;
    private boolean autoRenew;
    private LocalDateTime createdAt;

    public static SubscriptionResponse fromEntity(com.aiinterview.backend.entity.Subscription sub, String planName) {
        return SubscriptionResponse.builder()
                .id(sub.getId())
                .planName(planName)
                .subscriptionStatus(sub.getSubscriptionStatus())
                .amountPaid(sub.getAmountPaid())
                .currency(sub.getCurrency())
                .paymentMethod(sub.getPaymentMethod())
                .subscribedAt(sub.getSubscribedAt())
                .expiresAt(sub.getExpiresAt())
                .cancelledAt(sub.getCancelledAt())
                .autoRenew(sub.isAutoRenew())
                .createdAt(sub.getCreatedAt())
                .build();
    }
}
