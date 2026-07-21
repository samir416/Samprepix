package com.aiinterview.backend.service.impl;

import com.aiinterview.backend.service.EmailService;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
public class EmailServiceImpl implements EmailService {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String fromEmail;

    public EmailServiceImpl(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    @Override
    public void sendPasswordResetEmail(
            String to,
            String username,
            String resetLink) {

        try {

            MimeMessage message =
                    mailSender.createMimeMessage();

            MimeMessageHelper helper =
                    new MimeMessageHelper(
                            message,
                            true,
                            "UTF-8");

            helper.setFrom(fromEmail);
            helper.setTo(to);
            helper.setSubject("Reset Your Password");

            helper.setText(buildResetPasswordTemplate(
                    username,
                    resetLink
            ), true);

            mailSender.send(message);

        } catch (MessagingException ex) {

            throw new RuntimeException(
                    "Failed to send reset password email.",
                    ex
            );
        }
    }

    private String buildResetPasswordTemplate(
            String username,
            String resetLink) {

        return """
                <!DOCTYPE html>
                <html>
                <body style="
                    margin:0;
                    padding:40px;
                    background:#f4f7fb;
                    font-family:Arial,sans-serif;">

                    <div style="
                        max-width:620px;
                        margin:auto;
                        background:#ffffff;
                        border-radius:16px;
                        padding:40px;">

                        <h2 style="
                            margin-top:0;
                            color:#111827;">
                            Reset your password
                        </h2>

                        <p style="
                            color:#4b5563;
                            font-size:15px;">
                            Hi <b>%s</b>,
                        </p>

                        <p style="
                            color:#4b5563;
                            font-size:15px;
                            line-height:1.7;">
                            We received a request to reset your password.
                            Click the button below to create a new password.
                        </p>

                        <div style="margin:35px 0;">

                            <a href="%s"
                               style="
                                background:#2563eb;
                                color:#ffffff;
                                text-decoration:none;
                                padding:14px 28px;
                                border-radius:10px;
                                display:inline-block;
                                font-weight:bold;">

                                Reset Password

                            </a>

                        </div>

                        <p style="
                            color:#6b7280;
                            font-size:14px;">
                            This link expires in
                            <b>30 minutes</b>.
                        </p>

                        <hr style="
                            margin:30px 0;
                            border:none;
                            border-top:1px solid #e5e7eb;">

                        <p style="
                            color:#9ca3af;
                            font-size:13px;">

                            AI Interview & Placement Preparation Platform

                        </p>

                    </div>

                </body>
                </html>
                """.formatted(
                username,
                resetLink
        );
    }
}