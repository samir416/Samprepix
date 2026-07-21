package com.aiinterview.backend.service;

public interface EmailService {

    void sendPasswordResetEmail(
            String to,
            String username,
            String resetLink
    );
}