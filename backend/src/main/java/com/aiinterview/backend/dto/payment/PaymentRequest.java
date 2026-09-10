package com.aiinterview.backend.dto.payment;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentRequest {
    private String planId;
    private String currency;
    private String paymentMethod;
    private String referralCode; // Optional referral code for backend-validated discount
}
