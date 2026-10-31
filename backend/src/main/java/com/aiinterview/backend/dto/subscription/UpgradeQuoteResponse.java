package com.aiinterview.backend.dto.subscription;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpgradeQuoteResponse {

    private boolean eligible;
    private String reason;
    private String currentPlan;
    private String targetPlan;
    private double currentPaidAmount;
    private double targetPlanPrice;
    private double upgradeAmount;
    private String currency;
    private LocalDateTime originalExpiresAt;
    private long remainingDays;
    private String notificationMessage;
    private Long activeSubscriptionId;
}
