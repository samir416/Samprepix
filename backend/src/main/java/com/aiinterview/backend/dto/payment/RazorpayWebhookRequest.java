package com.aiinterview.backend.dto.payment;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RazorpayWebhookRequest {
    private String event;
    private String paymentId;
    private String orderId;
    private String signature;
    private String status;
    private double amount;
    private String currency;
    private String key;
}
