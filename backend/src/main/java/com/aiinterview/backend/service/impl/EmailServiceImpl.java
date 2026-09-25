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
    public void sendPasswordResetEmail(String to, String username, String resetLink) {
        validateRecipient(to);
        validateValue(username, "Username");
        validateValue(resetLink, "Reset link");

        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail);
            helper.setTo(to.trim());
            helper.setSubject("Reset Your Password");
            helper.setText(
                    buildResetPasswordTemplate(
                            escapeHtml(username),
                            escapeHtmlAttribute(resetLink)
                    ),
                    true
            );

            mailSender.send(message);
        } catch (MessagingException ex) {
            throw new RuntimeException("Failed to send reset password email.", ex);
        }
    }

    @Override
    public void sendOtpEmail(String to, String username, String otp) {
        validateRecipient(to);
        validateValue(username, "Username");
        validateOtp(otp);

        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail);
            helper.setTo(to.trim());
            helper.setSubject("Verify Your Email");
            helper.setText(
                    buildOtpTemplate(
                            escapeHtml(username),
                            escapeHtml(otp)
                    ),
                    true
            );

            mailSender.send(message);
        } catch (MessagingException ex) {
            throw new RuntimeException("Failed to send verification email.", ex);
        }
    }

    @Override
    public void sendPremiumAccessGrantedEmail(
            String to,
            String username,
            String planName
    ) {
        validateRecipient(to);
        validateValue(username, "Username");

        String normalizedPlan = normalizePlanName(planName);

        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail);
            helper.setTo(to.trim());
            helper.setSubject(
                    "Congratulations! Your "
                            + normalizedPlan
                            + " Access Is Now Unlocked"
            );
            helper.setText(
                    buildPremiumAccessTemplate(
                            escapeHtml(username),
                            normalizedPlan
                    ),
                    true
            );

            mailSender.send(message);
        } catch (MessagingException ex) {
            throw new RuntimeException("Failed to send premium access email.", ex);
        }
    }

    @Override
    public void sendManualPremiumAccessEmail(
            String to,
            String subject,
            String body
    ) {
        validateRecipient(to);
        validateValue(subject, "Email subject");
        validateValue(body, "Email body");

        String normalizedSubject = subject.trim();

        if (normalizedSubject.length() > 200) {
            throw new IllegalArgumentException("Email subject is too long.");
        }

        if (body.length() > 10000) {
            throw new IllegalArgumentException("Email body is too long.");
        }

        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail);
            helper.setTo(to.trim());
            helper.setSubject(normalizedSubject);
            helper.setText(buildManualEmailTemplate(body), true);

            mailSender.send(message);
        } catch (MessagingException ex) {
            throw new RuntimeException("Failed to send manual email.", ex);
        }
    }

    @Override
    public void sendProblemReportEmail(
            String to,
            String reporterEmail,
            String username,
            String feature,
            String description,
            String actionAttempted,
            String pageUrl
    ) {
        validateRecipient(to);
        validateValue(feature, "Feature");
        validateValue(description, "Description");

        String safeUsername = username != null && !username.isBlank() ? escapeHtml(username.trim()) : "Anonymous / Guest";
        String safeReporterEmail = reporterEmail != null && !reporterEmail.isBlank() ? escapeHtml(reporterEmail.trim()) : "Not provided";
        String safeFeature = escapeHtml(feature.trim());
        String safeDescription = escapeHtml(description.trim()).replace("\r\n", "\n").replace("\n", "<br>");
        String safeAction = actionAttempted != null && !actionAttempted.isBlank()
                ? escapeHtml(actionAttempted.trim()).replace("\r\n", "\n").replace("\n", "<br>")
                : "Not specified";
        String safeUrl = pageUrl != null && !pageUrl.isBlank() ? escapeHtml(pageUrl.trim()) : "N/A";

        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail);
            helper.setTo(to.trim());
            helper.setSubject("[Samprepix Bug Report] Issue in " + feature.trim());
            helper.setText(
                    buildProblemReportTemplate(
                            safeUsername,
                            safeReporterEmail,
                            safeFeature,
                            safeDescription,
                            safeAction,
                            safeUrl
                    ),
                    true
            );

            mailSender.send(message);
        } catch (MessagingException ex) {
            throw new RuntimeException("Failed to send problem report email.", ex);
        }
    }

    private String buildResetPasswordTemplate(
            String username,
            String resetLink
    ) {
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
                            This link expires in <b>30 minutes</b>.
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
                """.formatted(username, resetLink);
    }

    private String buildOtpTemplate(
            String username,
            String otp
    ) {
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
                            This verification code is valid for <b>10 minutes</b>.
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
                """.formatted(username, otp);
    }

    private String buildPremiumAccessTemplate(
            String username,
            String planName
    ) {
        String planLabel = "ELITE".equalsIgnoreCase(planName)
                ? "Elite"
                : "PRO".equalsIgnoreCase(planName)
                ? "Pro"
                : planName;

        String benefits = "ELITE".equalsIgnoreCase(planName)
                ? """
                  <li>Advanced AI-powered preparation</li>
                  <li>Premium interview and coding capabilities</li>
                  <li>Advanced performance insights</li>
                  <li>Priority platform capabilities</li>
                  <li>One month of Elite access</li>
                  """
                : """
                  <li>Premium AI-powered preparation</li>
                  <li>Enhanced interview and coding capabilities</li>
                  <li>Advanced preparation features</li>
                  <li>One month of Pro access</li>
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
                                    One month of premium access
                                </div>
                            </div>

                            <p style="
                                color:#374151;
                                font-size:15px;
                                line-height:1.8;">
                                We are pleased to let you know that premium
                                access has been granted to your account.
                                Your access is valid for one month according
                                to the premium access period.
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
                benefits
        );
    }

    private String buildManualEmailTemplate(String body) {
        String safeBody = escapeHtml(body)
                .replace("\r\n", "\n")
                .replace("\r", "\n")
                .replace("\n", "<br>");

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
                """.formatted(safeBody);
    }

    private String buildProblemReportTemplate(
            String username,
            String reporterEmail,
            String feature,
            String description,
            String actionAttempted,
            String pageUrl
    ) {
        String timestamp = java.time.LocalDateTime.now()
                .format(java.time.format.DateTimeFormatter.ofPattern("dd MMM yyyy, hh:mm a"));

        return """
                <!DOCTYPE html>
                <html>
                <body style="
                    margin:0;
                    padding:32px;
                    background:#0f172a;
                    font-family:-apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, Helvetica, Arial, sans-serif;
                    color:#f1f5f9;">

                    <div style="
                        max-width:680px;
                        margin:0 auto;
                        background:#1e293b;
                        border:1px solid #334155;
                        border-radius:18px;
                        overflow:hidden;
                        box-shadow:0 20px 40px rgba(0,0,0,0.4);">

                        <!-- HEADER -->
                        <div style="
                            padding:28px 36px;
                            background:linear-gradient(135deg, #1e1b4b, #312e81);
                            border-bottom:1px solid #4338ca;">
                            <div style="
                                font-size:12px;
                                font-weight:700;
                                letter-spacing:2px;
                                text-transform:uppercase;
                                color:#818cf8;">
                                SAMPREPIX PLATFORM SUPPORT
                            </div>
                            <h2 style="
                                margin:12px 0 4px;
                                font-size:24px;
                                color:#ffffff;">
                                User Problem / Bug Report
                            </h2>
                            <p style="
                                margin:0;
                                font-size:14px;
                                color:#cbd5e1;">
                                Reported at %s
                            </p>
                        </div>

                        <!-- BODY -->
                        <div style="padding:32px 36px;">

                            <!-- REPORTER INFO -->
                            <div style="
                                display:flex;
                                background:#0f172a;
                                border:1px solid #334155;
                                border-radius:12px;
                                padding:16px 20px;
                                margin-bottom:24px;">
                                <table style="width:100%%; border-collapse:collapse; color:#cbd5e1; font-size:14px;">
                                    <tr>
                                        <td style="padding:6px 0; width:140px; color:#94a3b8; font-weight:600;">Reported By:</td>
                                        <td style="padding:6px 0; color:#f8fafc; font-weight:600;">%s (%s)</td>
                                    </tr>
                                    <tr>
                                        <td style="padding:6px 0; color:#94a3b8; font-weight:600;">Feature / Area:</td>
                                        <td style="padding:6px 0;">
                                            <span style="
                                                background:#4338ca;
                                                color:#e0e7ff;
                                                padding:4px 12px;
                                                border-radius:999px;
                                                font-size:12px;
                                                font-weight:700;">
                                                %s
                                            </span>
                                        </td>
                                    </tr>
                                    <tr>
                                        <td style="padding:6px 0; color:#94a3b8; font-weight:600;">Page URL:</td>
                                        <td style="padding:6px 0; font-family:monospace; color:#38bdf8;">%s</td>
                                    </tr>
                                </table>
                            </div>

                            <!-- ACTION ATTEMPTED -->
                            <div style="margin-bottom:24px;">
                                <div style="
                                    font-size:13px;
                                    text-transform:uppercase;
                                    letter-spacing:1px;
                                    font-weight:700;
                                    color:#94a3b8;
                                    margin-bottom:8px;">
                                    What user was trying to do:
                                </div>
                                <div style="
                                    background:#0f172a;
                                    border:1px solid #334155;
                                    border-left:4px solid #6366f1;
                                    border-radius:8px;
                                    padding:14px 18px;
                                    color:#e2e8f0;
                                    font-size:14px;
                                    line-height:1.6;">
                                    %s
                                </div>
                            </div>

                            <!-- PROBLEM DESCRIPTION -->
                            <div style="margin-bottom:28px;">
                                <div style="
                                    font-size:13px;
                                    text-transform:uppercase;
                                    letter-spacing:1px;
                                    font-weight:700;
                                    color:#f87171;
                                    margin-bottom:8px;">
                                    Problem Description / What went wrong:
                                </div>
                                <div style="
                                    background:#0f172a;
                                    border:1px solid #334155;
                                    border-left:4px solid #ef4444;
                                    border-radius:8px;
                                    padding:16px 18px;
                                    color:#f8fafc;
                                    font-size:14px;
                                    line-height:1.7;">
                                    %s
                                </div>
                            </div>

                            <hr style="margin:28px 0; border:none; border-top:1px solid #334155;">

                            <p style="
                                margin:0;
                                color:#64748b;
                                font-size:12px;
                                text-align:center;">
                                Automated notification sent from Samprepix User Feedback & Issue Reporter.
                            </p>
                        </div>
                    </div>
                </body>
                </html>
                """.formatted(
                timestamp,
                username,
                reporterEmail,
                feature,
                pageUrl,
                actionAttempted,
                description
        );
    }

    private void validateRecipient(String email) {
        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException("Recipient email is required.");
        }

        String normalized = email.trim();

        if (normalized.length() > 254
                || normalized.contains("\r")
                || normalized.contains("\n")
                || !normalized.matches("^[A-Za-z0-9.!#$%&'*+/=?^_`{|}~-]+@[A-Za-z0-9-]+(?:\\.[A-Za-z0-9-]+)+$")) {
            throw new IllegalArgumentException("Invalid recipient email.");
        }
    }

    private void validateValue(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(fieldName + " is required.");
        }
    }

    private void validateOtp(String otp) {
        if (otp == null || !otp.matches("\\d{4}")) {
            throw new IllegalArgumentException("Invalid OTP.");
        }
    }

    private String normalizePlanName(String planName) {
        if (planName == null || planName.isBlank()) {
            return "PREMIUM";
        }

        String normalized = planName.trim().toUpperCase();

        if (!normalized.equals("PRO")
                && !normalized.equals("ELITE")
                && !normalized.equals("PREMIUM")) {
            throw new IllegalArgumentException("Invalid premium plan.");
        }

        return normalized;
    }

    private String escapeHtml(String value) {
        if (value == null) {
            return "";
        }

        return value
                .replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&#39;");
    }

    private String escapeHtmlAttribute(String value) {
        return escapeHtml(value);
    }
}