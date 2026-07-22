package com.aiinterview.backend.service;

public interface EmailService {

    void sendPasswordResetEmail(
            String to,
            String username,
            String resetLink
    );

    void sendOtpEmail(
        String to,
        String username,
        String otp
);

}