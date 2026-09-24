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

    private String cashfreeSessionId;

    private String cashfreePaymentId;

    private double amount;

    private double displayAmount;

    private String currency;

    private String key;

    private String status;

    private String planName;

    private String subscriptionPeriod;

    private boolean autoRenew;

    private boolean lifetime;

    private String referralCode;

    private double discountAmount;

    private String paymentMethod;
}