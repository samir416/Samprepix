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
                            "UTF-8"
                    );

            helper.setFrom(fromEmail);
            helper.setTo(to);
            helper.setSubject("Reset Your Password");

            helper.setText(
                    buildResetPasswordTemplate(
                            username,
                            resetLink
                    ),
                    true
            );

            mailSender.send(message);

        } catch (MessagingException ex) {

            throw new RuntimeException(
                    "Failed to send reset password email.",
                    ex
            );
        }
    }

    @Override
    public void sendOtpEmail(
            String to,
            String username,
            String otp) {

        try {

            MimeMessage message =
                    mailSender.createMimeMessage();

            MimeMessageHelper helper =
                    new MimeMessageHelper(
                            message,
                            true,
                            "UTF-8"
                    );

            helper.setFrom(fromEmail);
            helper.setTo(to);
            helper.setSubject("Verify Your Email");

            helper.setText(
                    buildOtpTemplate(
                            username,
                            otp
                    ),
                    true
            );

            mailSender.send(message);

        } catch (MessagingException ex) {

            throw new RuntimeException(
                    "Failed to send verification email.",
                    ex
            );
        }
    }

    @Override
    public void sendPremiumAccessGrantedEmail(
            String to,
            String username,
            String planName) {

        try {

            MimeMessage message =
                    mailSender.createMimeMessage();

            MimeMessageHelper helper =
                    new MimeMessageHelper(
                            message,
                            true,
                            "UTF-8"
                    );

            String normalizedPlan =
                    planName == null
                            ? "PREMIUM"
                            : planName.trim().toUpperCase();

            helper.setFrom(fromEmail);
            helper.setTo(to);
            helper.setSubject(
                    "Congratulations! Your "
                            + normalizedPlan
                            + " Access Is Now Unlocked"
            );

            helper.setText(
                    buildPremiumAccessTemplate(
                            username,
                            normalizedPlan
                    ),
                    true
            );

            mailSender.send(message);

        } catch (MessagingException ex) {

            throw new RuntimeException(
                    "Failed to send premium access email.",
                    ex
            );
        }
    }

    @Override
    public void sendManualPremiumAccessEmail(
            String to,
            String subject,
            String body) {

        if (to == null || to.isBlank()) {
            throw new IllegalArgumentException(
                    "Recipient email is required."
            );
        }

        if (subject == null || subject.isBlank()) {
            throw new IllegalArgumentException(
                    "Email subject is required."
            );
        }

        if (body == null || body.isBlank()) {
            throw new IllegalArgumentException(
                    "Email body is required."
            );
        }

        try {

            MimeMessage message =
                    mailSender.createMimeMessage();

            MimeMessageHelper helper =
                    new MimeMessageHelper(
                            message,
                            true,
                            "UTF-8"
                    );

            helper.setFrom(fromEmail);
            helper.setTo(to);
            helper.setSubject(subject.trim());

            helper.setText(
                    buildManualEmailTemplate(body),
                    true
            );

            mailSender.send(message);

        } catch (MessagingException ex) {

            throw new RuntimeException(
                    "Failed to send manual email.",
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

    private String buildOtpTemplate(
            String username,
            String otp) {

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
                            Verify Your Email
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
                            Thank you for creating your account.
                            Please use the verification code below to activate your account.
                        </p>

                        <div style="
                            margin:35px 0;
                            text-align:center;">

                            <div style="
                                display:inline-block;
                                padding:16px 40px;
                                background:#2563eb;
                                color:#ffffff;
                                border-radius:12px;
                                font-size:32px;
                                font-weight:bold;
                                letter-spacing:10px;">

                                %s

                            </div>

                        </div>

                        <p style="
                            color:#6b7280;
                            font-size:14px;">
                            This verification code is valid for
                            <b>10 minutes</b>.
                        </p>

                        <p style="
                            color:#6b7280;
                            font-size:14px;
                            line-height:1.7;">
                            If you did not create this account,
                            you can safely ignore this email.
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
                otp
        );
    }

    private String buildPremiumAccessTemplate(
            String username,
            String planName) {

        String planLabel =
                "ELITE".equalsIgnoreCase(planName)
                        ? "Elite"
                        : "PRO".equalsIgnoreCase(planName)
                        ? "Pro"
                        : planName;

        String benefits =
                "ELITE".equalsIgnoreCase(planName)
                        ? """
                          <li>Advanced AI-powered preparation</li>
                          <li>Premium interview and coding capabilities</li>
                          <li>Advanced performance insights</li>
                          <li>Priority platform capabilities</li>
                          <li>Lifetime Elite access</li>
                          """
                        : """
                          <li>Premium AI-powered preparation</li>
                          <li>Enhanced interview and coding capabilities</li>
                          <li>Advanced preparation features</li>
                          <li>Lifetime Pro access</li>
                          """;

        return """
                <!DOCTYPE html>
                <html>
                <body style="
                    margin:0;
                    padding:32px;
                    background:#f3f6fb;
                    font-family:Arial,Helvetica,sans-serif;">

                    <div style="
                        max-width:680px;
                        margin:0 auto;
                        background:#ffffff;
                        border-radius:22px;
                        overflow:hidden;
                        box-shadow:0 10px 35px rgba(15,23,42,0.08);">

                        <div style="
                            padding:34px 40px;
                            background:#111827;
                            color:#ffffff;">

                            <div style="
                                font-size:13px;
                                letter-spacing:2px;
                                text-transform:uppercase;
                                opacity:0.75;">
                                AI Interview & Placement Preparation Platform
                            </div>

                            <h1 style="
                                margin:18px 0 8px;
                                font-size:30px;">
                                Congratulations, %s!
                            </h1>

                            <p style="
                                margin:0;
                                font-size:16px;
                                line-height:1.6;
                                opacity:0.9;">
                                Your %s experience has been successfully unlocked.
                            </p>

                        </div>

                        <div style="padding:40px;">

                            <div style="
                                padding:22px;
                                border-radius:16px;
                                background:#f8fafc;
                                border:1px solid #e5e7eb;
                                margin-bottom:28px;">

                                <div style="
                                    font-size:12px;
                                    text-transform:uppercase;
                                    letter-spacing:1.5px;
                                    color:#64748b;">
                                    Access Unlocked
                                </div>

                                <div style="
                                    margin-top:8px;
                                    font-size:26px;
                                    font-weight:700;
                                    color:#111827;">
                                    %s Plan
                                </div>

                                <div style="
                                    margin-top:6px;
                                    color:#475569;
                                    font-size:14px;">
                                    Lifetime premium access
                                </div>

                            </div>

                            <p style="
                                color:#374151;
                                font-size:15px;
                                line-height:1.8;">
                                We are pleased to let you know that premium
                                access has been granted to your account.
                                You can now use the capabilities included
                                with your %s plan.
                            </p>

                            <h3 style="
                                margin-top:30px;
                                color:#111827;">
                                Your unlocked benefits
                            </h3>

                            <ul style="
                                padding-left:22px;
                                color:#475569;
                                font-size:15px;
                                line-height:2;">
                                %s
                            </ul>

                            <div style="
                                margin:32px 0;
                                text-align:center;">

                                <a href="#"
                                   style="
                                    display:inline-block;
                                    padding:14px 30px;
                                    border-radius:11px;
                                    background:#111827;
                                    color:#ffffff;
                                    text-decoration:none;
                                    font-weight:700;">
                                    Continue to Your Dashboard
                                </a>

                            </div>

                            <p style="
                                color:#64748b;
                                font-size:14px;
                                line-height:1.7;">
                                Thank you for being part of our platform.
                                We wish you continued success in your
                                interview preparation and career journey.
                            </p>

                            <hr style="
                                margin:30px 0;
                                border:none;
                                border-top:1px solid #e5e7eb;">

                            <p style="
                                margin:0;
                                color:#94a3b8;
                                font-size:12px;
                                line-height:1.6;">
                                This is an automated account notification.
                                If you believe this access was granted in error,
                                please contact the platform administration team.
                            </p>

                        </div>

                    </div>

                </body>
                </html>
                """.formatted(
                username,
                planLabel,
                planLabel,
                planLabel,
                benefits
        );
    }

    private String buildManualEmailTemplate(
            String body) {

        String safeBody =
                body.replace(
                        "\n",
                        "<br>"
                );

        return """
                <!DOCTYPE html>
                <html>
                <body style="
                    margin:0;
                    padding:32px;
                    background:#f3f6fb;
                    font-family:Arial,Helvetica,sans-serif;">

                    <div style="
                        max-width:680px;
                        margin:0 auto;
                        background:#ffffff;
                        border-radius:20px;
                        padding:40px;
                        box-shadow:0 10px 35px rgba(15,23,42,0.08);">

                        <div style="
                            color:#111827;
                            font-size:13px;
                            font-weight:700;
                            letter-spacing:1.5px;
                            text-transform:uppercase;
                            margin-bottom:28px;">
                            AI Interview & Placement Preparation Platform
                        </div>

                        <div style="
                            color:#374151;
                            font-size:15px;
                            line-height:1.8;">
                            %s
                        </div>

                        <hr style="
                            margin:34px 0;
                            border:none;
                            border-top:1px solid #e5e7eb;">

                        <p style="
                            margin:0;
                            color:#94a3b8;
                            font-size:12px;">
                            AI Interview & Placement Preparation Platform
                        </p>

                    </div>

                </body>
                </html>
                """.formatted(
                safeBody
        );
    }
}