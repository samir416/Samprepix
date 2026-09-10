package com.aiinterview.backend.dto.payment;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentResponse {
    private String orderId;
    private String razorpayOrderId;
    private double amount;
    private String currency;
    private String key;
    private String status;
    private String planName;
}
