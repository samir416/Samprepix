package com.aiinterview.backend.dto.payment;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentRequest {

    @NotBlank(message = "Plan ID is required")
    @Size(max = 30, message = "Plan ID is invalid")
    @Pattern(
            regexp = "^[A-Za-z0-9_-]+$",
            message = "Invalid plan ID"
    )
    private String planId;

    @NotBlank(message = "Currency is required")
    @Pattern(
            regexp = "(?i)^(INR|USD)$",
            message = "Only INR and USD are supported"
    )
    private String currency;

    @NotBlank(message = "Payment method is required")
    @Size(max = 30, message = "Payment method is invalid")
    @Pattern(
            regexp = "^[A-Za-z0-9_-]+$",
            message = "Invalid payment method"
    )
    private String paymentMethod;

    @Size(max = 50, message = "Referral code is invalid")
    @Pattern(
            regexp = "^[A-Za-z0-9_-]*$",
            message = "Invalid referral code"
    )
    private String referralCode;
}