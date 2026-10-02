package com.aiinterview.backend.service.impl;

import com.aiinterview.backend.service.EmailService;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.util.Locale;

@Service
public class EmailServiceImpl implements EmailService {

    private static final Logger log = LoggerFactory.getLogger(EmailServiceImpl.class);

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
    public void sendPaymentConfirmationEmail(
            String to,
            String customerName,
            String planName,
            Double amount,
            String currency,
            String orderReference,
            String paymentDate
    ) {
        validateRecipient(to);
        String name = (customerName != null && !customerName.isBlank()) ? customerName.trim() : "Valued Member";
        String normalizedPlan = normalizePlanName(planName);
        String formattedAmount = (currency != null && currency.equalsIgnoreCase("USD") ? "$" : "₹")
                + String.format(Locale.ROOT, "%.2f", (amount != null ? amount : 0.0));
        String dateStr = (paymentDate != null && !paymentDate.isBlank())
                ? paymentDate
                : java.time.LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("dd MMM yyyy, hh:mm a"));
        String ref = (orderReference != null && !orderReference.isBlank()) ? orderReference.trim() : "N/A";

        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail);
            helper.setTo(to.trim());
            helper.setSubject("Payment Confirmed: Your SamPrepIX " + normalizedPlan + " Plan Is Active");
            helper.setText(
                    buildPaymentConfirmationTemplate(
                            escapeHtml(name),
                            escapeHtml(to.trim()),
                            normalizedPlan,
                            formattedAmount,
                            escapeHtml(ref),
                            dateStr
                    ),
                    true
            );

            mailSender.send(message);
            log.info("Payment confirmation email successfully delivered to {}", to);
        } catch (MessagingException ex) {
            log.error("Failed to deliver payment confirmation email to {}: {}", to, ex.getMessage());
            throw new RuntimeException("Failed to send payment confirmation email.", ex);
        }
    }

    @Override
    public void sendSubscriptionRevokedEmail(
            String to,
            String customerName,
            String planName,
            Double refundAmount,
            String refundReference,
            String expectedTimeframe
    ) {
        validateRecipient(to);
        String name = (customerName != null && !customerName.isBlank()) ? customerName.trim() : "Valued Member";
        String normalizedPlan = normalizePlanName(planName);
        String formattedRefund = (refundAmount != null && refundAmount > 0)
                ? "₹" + String.format(Locale.ROOT, "%.2f", refundAmount)
                : "Eligible Amount";
        String timeframe = (expectedTimeframe != null && !expectedTimeframe.isBlank())
                ? expectedTimeframe
                : "Typically 5–7 business days depending on your bank and payment method";
        String ref = (refundReference != null && !refundReference.isBlank()) ? refundReference.trim() : "Initiated";

        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail);
            helper.setTo(to.trim());
            helper.setSubject("Your SamPrepIX Premium Plan Has Been Cancelled");
            helper.setText(
                    buildSubscriptionRevokedTemplate(
                            escapeHtml(name),
                            normalizedPlan,
                            formattedRefund,
                            escapeHtml(ref),
                            timeframe
                    ),
                    true
            );

            mailSender.send(message);
            log.info("Subscription revocation email successfully delivered to {}", to);
        } catch (MessagingException ex) {
            log.error("Failed to deliver subscription revocation email to {}: {}", to, ex.getMessage());
            throw new RuntimeException("Failed to send subscription revocation email.", ex);
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
                    padding:16px 12px;
                    background:#f4f7fb;
                    font-family:Arial,sans-serif;
                    -webkit-text-size-adjust:100%;
                    -ms-text-size-adjust:100%;">
                    <div style="
                        max-width:580px;
                        width:100%;
                        margin:auto;
                        background:#ffffff;
                        border-radius:16px;
                        padding:26px 20px;
                        word-break:break-word;
                        box-shadow:0 6px 20px rgba(15,23,42,0.06);
                        border:1px solid #e5e7eb;">
                        <h2 style="
                            margin-top:0;
                            font-size:22px;
                            color:#111827;">
                            Reset your password
                        </h2>

                        <p style="
                            color:#4b5563;
                            font-size:14px;">
                            Hi <b>%s</b>,
                        </p>

                        <p style="
                            color:#4b5563;
                            font-size:14px;
                            line-height:1.7;">
                            We received a request to reset your password.
                            Click the button below to create a new password.
                        </p>

                        <div style="margin:28px 0;">
                            <a href="%s"
                               style="
                                background:#2563eb;
                                color:#ffffff;
                                text-decoration:none;
                                padding:12px 24px;
                                border-radius:10px;
                                display:inline-block;
                                font-weight:bold;
                                font-size:14px;">
                                Reset Password
                            </a>
                        </div>

                        <p style="
                            color:#6b7280;
                            font-size:13px;">
                            This link expires in <b>30 minutes</b>.
                        </p>

                        <hr style="
                            margin:24px 0;
                            border:none;
                            border-top:1px solid #e5e7eb;">

                        <p style="
                            color:#9ca3af;
                            font-size:12px;">
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
                    padding:16px 12px;
                    background:#f4f7fb;
                    font-family:Arial,sans-serif;
                    -webkit-text-size-adjust:100%;
                    -ms-text-size-adjust:100%;">
                    <div style="
                        max-width:580px;
                        width:100%;
                        margin:auto;
                        background:#ffffff;
                        border-radius:16px;
                        padding:26px 20px;
                        word-break:break-word;
                        box-shadow:0 6px 20px rgba(15,23,42,0.06);
                        border:1px solid #e5e7eb;">
                        <h2 style="
                            margin-top:0;
                            font-size:22px;
                            color:#111827;">
                            Verify Your Email
                        </h2>

                        <p style="
                            color:#4b5563;
                            font-size:14px;">
                            Hi <b>%s</b>,
                        </p>

                        <p style="
                            color:#4b5563;
                            font-size:14px;
                            line-height:1.7;">
                            Thank you for creating your account.
                            Please use the verification code below to activate your account.
                        </p>

                        <div style="
                            margin:28px 0;
                            text-align:center;">
                            <div style="
                                display:inline-block;
                                padding:14px 30px;
                                background:#2563eb;
                                color:#ffffff;
                                border-radius:12px;
                                font-size:28px;
                                font-weight:bold;
                                letter-spacing:8px;">
                                %s
                            </div>
                        </div>

                        <p style="
                            color:#6b7280;
                            font-size:13px;">
                            This verification code is valid for <b>10 minutes</b>.
                        </p>

                        <p style="
                            color:#6b7280;
                            font-size:13px;
                            line-height:1.7;">
                            If you did not create this account,
                            you can safely ignore this email.
                        </p>

                        <hr style="
                            margin:24px 0;
                            border:none;
                            border-top:1px solid #e5e7eb;">

                        <p style="
                            color:#9ca3af;
                            font-size:12px;">
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
                    padding:16px 12px;
                    background:#f3f6fb;
                    font-family:Arial,Helvetica,sans-serif;
                    -webkit-text-size-adjust:100%;
                    -ms-text-size-adjust:100%;">

                    <table role="presentation" width="100%" cellspacing="0" cellpadding="0" border="0" style="
                        max-width:580px;
                        margin:0 auto;
                        background:#ffffff;
                        border-radius:18px;
                        overflow:hidden;
                        box-shadow:0 10px 35px rgba(15,23,42,0.08);
                        border:1px solid #e2e8f0;">
                        <tr>
                            <td>
                                <div style="
                                    padding:28px 24px;
                                    background:#111827;
                                    color:#ffffff;
                                    word-break:break-word;">

                                    <div style="
                                        font-size:12px;
                                        letter-spacing:1.5px;
                                        text-transform:uppercase;
                                        opacity:0.75;">
                                        AI Interview & Placement Platform
                                    </div>

                                    <h1 style="
                                        margin:14px 0 8px;
                                        font-size:24px;
                                        line-height:1.3;">
                                        Congratulations, %s!
                                    </h1>

                                    <p style="
                                        margin:0;
                                        font-size:15px;
                                        line-height:1.5;
                                        opacity:0.9;">
                                        Your %s experience has been successfully unlocked.
                                    </p>
                                </div>

                                <div style="padding:26px 22px;word-break:break-word;overflow-wrap:break-word;">

                                    <div style="
                                        padding:18px 20px;
                                        border-radius:14px;
                                        background:#f8fafc;
                                        border:1px solid #e5e7eb;
                                        margin-bottom:24px;">

                                        <div style="
                                            font-size:11px;
                                            text-transform:uppercase;
                                            letter-spacing:1.5px;
                                            color:#64748b;">
                                            Access Unlocked
                                        </div>

                                        <div style="
                                            margin-top:6px;
                                            font-size:22px;
                                            font-weight:700;
                                            color:#111827;">
                                            %s Plan
                                        </div>

                                        <div style="
                                            margin-top:4px;
                                            color:#475569;
                                            font-size:13px;">
                                            One month of premium platform access
                                        </div>
                                    </div>

                                    <p style="
                                        color:#374151;
                                        font-size:14px;
                                        line-height:1.7;">
                                        We are pleased to let you know that premium
                                        access has been granted to your account.
                                        Your access is active for one month.
                                    </p>

                                    <h3 style="
                                        margin-top:24px;
                                        color:#111827;
                                        font-size:16px;">
                                        Your Unlocked Benefits
                                    </h3>

                                    <ul style="
                                        padding-left:20px;
                                        color:#475569;
                                        font-size:14px;
                                        line-height:1.8;">
                                        %s
                                    </ul>

                                    <div style="
                                        margin:28px 0;
                                        text-align:center;">

                                        <a href="#"
                                           style="
                                            display:inline-block;
                                            padding:13px 26px;
                                            border-radius:10px;
                                            background:#111827;
                                            color:#ffffff;
                                            text-decoration:none;
                                            font-weight:700;
                                            font-size:14px;">
                                            Continue to Your Dashboard
                                        </a>
                                    </div>

                                    <p style="
                                        color:#64748b;
                                        font-size:13px;
                                        line-height:1.6;">
                                        Thank you for being part of SamPrepIX.
                                        We wish you continued success in your placement preparation!
                                    </p>

                                    <hr style="
                                        margin:24px 0;
                                        border:none;
                                        border-top:1px solid #e5e7eb;">

                                    <p style="
                                        margin:0;
                                        color:#94a3b8;
                                        font-size:11px;
                                        line-height:1.5;">
                                        This is an automated account notification from SamPrepIX.
                                    </p>
                                </div>
                            </td>
                        </tr>
                    </table>
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
                    padding:16px 12px;
                    background:#0f172a;
                    font-family:-apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, Helvetica, Arial, sans-serif;
                    color:#f1f5f9;
                    -webkit-text-size-adjust:100%;
                    -ms-text-size-adjust:100%;">

                    <div style="
                        max-width:580px;
                        width:100%;
                        margin:0 auto;
                        background:#1e293b;
                        border:1px solid #334155;
                        border-radius:16px;
                        overflow:hidden;
                        box-shadow:0 20px 40px rgba(0,0,0,0.4);">

                        <!-- HEADER -->
                        <div style="
                            padding:22px 20px;
                            background:linear-gradient(135deg, #1e1b4b, #312e81);
                            border-bottom:1px solid #4338ca;
                            word-break:break-word;">
                            <div style="
                                font-size:11px;
                                font-weight:700;
                                letter-spacing:2px;
                                text-transform:uppercase;
                                color:#818cf8;">
                                SAMPREPIX PLATFORM SUPPORT
                            </div>
                            <h2 style="
                                margin:10px 0 4px;
                                font-size:20px;
                                color:#ffffff;">
                                User Problem / Bug Report
                            </h2>
                            <p style="
                                margin:0;
                                font-size:13px;
                                color:#cbd5e1;">
                                Reported at %s
                            </p>
                        </div>

                        <!-- BODY -->
                        <div style="padding:22px 20px;word-break:break-word;overflow-wrap:break-word;">

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

    private String buildPaymentConfirmationTemplate(
            String customerName,
            String customerEmail,
            String planName,
            String formattedAmount,
            String orderReference,
            String paymentDate
    ) {
        return """
                <!DOCTYPE html>
                <html>
                <body style="margin:0;padding:16px 12px;background:#f8fafc;font-family:Arial,Helvetica,sans-serif;-webkit-text-size-adjust:100%%;-ms-text-size-adjust:100%%;">
                    <table role="presentation" width="100%%" cellspacing="0" cellpadding="0" border="0" style="max-width:580px;margin:0 auto;background:#ffffff;border-radius:16px;box-shadow:0 10px 30px rgba(15,23,42,0.06);border:1px solid #e2e8f0;overflow:hidden;">
                        <tr>
                            <td style="padding:28px 24px;word-break:break-word;overflow-wrap:break-word;">
                                <div style="margin-bottom:20px;">
                                    <span style="font-size:22px;font-weight:800;color:#1e293b;letter-spacing:-0.5px;">SamPrepIX</span>
                                </div>
                                <div style="padding:16px 18px;background:#f0fdf4;border-radius:12px;border:1px solid #bbf7d0;margin-bottom:22px;">
                                    <h2 style="margin:0 0 6px 0;color:#166534;font-size:18px;font-weight:700;">Payment Successful</h2>
                                    <p style="margin:0;color:#15803d;font-size:14px;line-height:1.5;">Thank you, %s! Your premium subscription has been activated.</p>
                                </div>
                                <table style="width:100%%;border-collapse:collapse;margin-bottom:22px;font-size:14px;word-break:break-word;">
                                    <tr style="border-bottom:1px solid #f1f5f9;">
                                        <td style="padding:12px 0;color:#64748b;">Plan</td>
                                        <td style="padding:12px 0;text-align:right;font-weight:700;color:#1e293b;">%s</td>
                                    </tr>
                                    <tr style="border-bottom:1px solid #f1f5f9;">
                                        <td style="padding:12px 0;color:#64748b;">Amount Paid</td>
                                        <td style="padding:12px 0;text-align:right;font-weight:700;color:#1e293b;">%s</td>
                                    </tr>
                                    <tr style="border-bottom:1px solid #f1f5f9;">
                                        <td style="padding:12px 0;color:#64748b;">Order Reference</td>
                                        <td style="padding:12px 0;text-align:right;font-weight:600;color:#475569;word-break:break-all;">%s</td>
                                    </tr>
                                    <tr style="border-bottom:1px solid #f1f5f9;">
                                        <td style="padding:12px 0;color:#64748b;">Date</td>
                                        <td style="padding:12px 0;text-align:right;color:#475569;">%s</td>
                                    </tr>
                                    <tr>
                                        <td style="padding:12px 0;color:#64748b;">Status</td>
                                        <td style="padding:12px 0;text-align:right;font-weight:700;color:#16a34a;">Completed</td>
                                    </tr>
                                </table>
                                <div style="background:#f8fafc;padding:16px 18px;border-radius:12px;margin-bottom:22px;">
                                    <h4 style="margin:0 0 6px 0;color:#1e293b;font-size:14px;">What's Next?</h4>
                                    <p style="margin:0;color:#475569;font-size:13px;line-height:1.6;">You can immediately start practicing with full Coding Arena access, AI Mock Interviews, and customized AI Career Roadmaps.</p>
                                </div>
                                <hr style="margin:22px 0;border:none;border-top:1px solid #e2e8f0;">
                                <p style="margin:0;color:#94a3b8;font-size:12px;line-height:1.5;">This email was sent to %s. SamPrepIX • AI-Powered Placement Preparation Platform</p>
                            </td>
                        </tr>
                    </table>
                </body>
                </html>
                """.formatted(customerName, planName, formattedAmount, orderReference, paymentDate, customerEmail);
    }

    private String buildSubscriptionRevokedTemplate(
            String customerName,
            String planName,
            String formattedRefund,
            String refundReference,
            String timeframe
    ) {
        return """
                <!DOCTYPE html>
                <html>
                <body style="margin:0;padding:16px 12px;background:#f8fafc;font-family:Arial,Helvetica,sans-serif;-webkit-text-size-adjust:100%%;-ms-text-size-adjust:100%%;">
                    <table role="presentation" width="100%%" cellspacing="0" cellpadding="0" border="0" style="max-width:580px;margin:0 auto;background:#ffffff;border-radius:16px;box-shadow:0 10px 30px rgba(15,23,42,0.06);border:1px solid #e2e8f0;overflow:hidden;">
                        <tr>
                            <td style="padding:28px 24px;word-break:break-word;overflow-wrap:break-word;">
                                <div style="margin-bottom:20px;">
                                    <span style="font-size:22px;font-weight:800;color:#1e293b;letter-spacing:-0.5px;">SamPrepIX</span>
                                </div>
                                <div style="padding:16px 18px;background:#fef2f2;border-radius:12px;border:1px solid #fecaca;margin-bottom:22px;">
                                    <h2 style="margin:0 0 6px 0;color:#991b1b;font-size:18px;font-weight:700;">Subscription Notice</h2>
                                    <p style="margin:0;color:#b91c1c;font-size:14px;line-height:1.5;">Hello %s, your SamPrepIX %s Plan has been cancelled.</p>
                                </div>
                                <p style="color:#334155;font-size:14px;line-height:1.7;margin-bottom:18px;">
                                    A refund for your eligible payment has been initiated through our payment provider. The refund will be processed back to your original payment method.
                                </p>
                                <table style="width:100%%;border-collapse:collapse;margin-bottom:22px;font-size:14px;word-break:break-word;">
                                    <tr style="border-bottom:1px solid #f1f5f9;">
                                        <td style="padding:12px 0;color:#64748b;">Refund Amount</td>
                                        <td style="padding:12px 0;text-align:right;font-weight:700;color:#1e293b;">%s</td>
                                    </tr>
                                    <tr style="border-bottom:1px solid #f1f5f9;">
                                        <td style="padding:12px 0;color:#64748b;">Refund Reference</td>
                                        <td style="padding:12px 0;text-align:right;font-weight:600;color:#475569;word-break:break-all;">%s</td>
                                    </tr>
                                    <tr style="border-bottom:1px solid #f1f5f9;">
                                        <td style="padding:12px 0;color:#64748b;">Expected Timeframe</td>
                                        <td style="padding:12px 0;text-align:right;color:#475569;">%s</td>
                                    </tr>
                                    <tr>
                                        <td style="padding:12px 0;color:#64748b;">Original Payment Method</td>
                                        <td style="padding:12px 0;text-align:right;font-weight:600;color:#475569;">Original Instrument (UPI / Card / Net Banking)</td>
                                    </tr>
                                </table>
                                <div style="background:#f8fafc;padding:16px 18px;border-radius:12px;margin-bottom:22px;">
                                    <h4 style="margin:0 0 6px 0;color:#1e293b;font-size:14px;">Next Steps</h4>
                                    <p style="margin:0;color:#475569;font-size:13px;line-height:1.6;">Your account remains active on the Starter tier. You can continue accessing all free resources, and you may purchase a premium plan again if you wish.</p>
                                </div>
                                <hr style="margin:22px 0;border:none;border-top:1px solid #e2e8f0;">
                                <p style="margin:0;color:#94a3b8;font-size:12px;line-height:1.5;">SamPrepIX • Subscription & Refund Policy. For details, visit your account dashboard.</p>
                            </td>
                        </tr>
                    </table>
                </body>
                </html>
                """.formatted(customerName, planName, formattedRefund, refundReference, timeframe);
    }
}