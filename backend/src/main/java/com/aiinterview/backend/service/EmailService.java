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

    void sendPremiumAccessGrantedEmail(
            String to,
            String username,
            String planName
    );

    void sendManualPremiumAccessEmail(
            String to,
            String subject,
            String body
    );

    void sendProblemReportEmail(
            String to,
            String reporterEmail,
            String username,
            String feature,
            String description,
            String actionAttempted,
            String pageUrl
    );
}