package com.aiinterview.backend.service.feedback;

import com.aiinterview.backend.entity.FeedbackApprovalToken;
import com.aiinterview.backend.entity.InterviewFeedback;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.time.format.DateTimeFormatter;

@Service
public class FeedbackModerationEmailServiceImpl
        implements FeedbackModerationEmailService {

    private final JavaMailSender mailSender;

    @Value("${feedback.moderation.owner-email}")
    private String ownerEmail;

    @Value("${feedback.moderation.base-url}")
    private String baseUrl;

    @Value("${spring.mail.username}")
    private String senderEmail;

    public FeedbackModerationEmailServiceImpl(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    @Override
    public void sendFeedbackForApproval(
            InterviewFeedback feedback,
            FeedbackApprovalToken token) {

        try {

            MimeMessage message = mailSender.createMimeMessage();

            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(senderEmail);
            helper.setTo(ownerEmail);
            helper.setSubject("New Interview Feedback Requires Approval");

            String approveUrl = baseUrl + "/api/feedback/approve?token=" + token.getToken();

            String rejectUrl = baseUrl + "/api/feedback/reject?token=" + token.getToken();

            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd MMM yyyy hh:mm a");

            String suggestion = (feedback.getSuggestion() == null ||
                    feedback.getSuggestion().trim().isEmpty())
                            ? "No suggestion provided."
                            : feedback.getSuggestion();

            String html = """
                          <!DOCTYPE html>
                          <html>
                          <head>
                              <meta charset="UTF-8">
                              <title>Feedback Moderation</title>
                          </head>

                          <body style="margin:0;padding:30px;background:#f5f5f5;font-family:Arial,sans-serif;">

                          <div style="max-width:700px;margin:auto;background:#ffffff;
                                      border-radius:10px;overflow:hidden;
                                      box-shadow:0 2px 10px rgba(0,0,0,.15);">

                              <div style="background:#2563eb;
                                          color:#ffffff;
                                          padding:20px;
                                          text-align:center;">

                                  <h2 style="margin:0;">
                                      AI Interview & Placement Preparation Platform
                                  </h2>

                                  <p style="margin-top:8px;">
                                      New Interview Feedback Requires Approval
                                  </p>

                              </div>

                              <div style="padding:30px;">

                                  <h3 style="margin-top:0;color:#1f2937;">
                                      Interview Feedback Details
                                  </h3>

                                 <table style="width:100%%;
                    border-collapse:collapse;
                    margin-top:20px;">
                                      <tr>
                                          <td style="padding:12px;
                                                     font-weight:bold;
                                                     width:180px;
                                                     border-bottom:1px solid #e5e7eb;">
                                              User Name
                                          </td>

                                          <td style="padding:12px;
                                                     border-bottom:1px solid #e5e7eb;">
                                              %s
                                          </td>
                                      </tr>

                                      <tr>
                                          <td style="padding:12px;
                                                     font-weight:bold;
                                                     border-bottom:1px solid #e5e7eb;">
                                              Email
                                          </td>

                                          <td style="padding:12px;
                                                     border-bottom:1px solid #e5e7eb;">
                                              %s
                                          </td>
                                      </tr>

                                      <tr>
                                          <td style="padding:12px;
                                                     font-weight:bold;
                                                     border-bottom:1px solid #e5e7eb;">
                                              Rating
                                          </td>

                                          <td style="padding:12px;
                                                     border-bottom:1px solid #e5e7eb;">
                                              ⭐ %s / 5
                                          </td>
                                      </tr>

                                      <tr>
                                          <td style="padding:12px;
                                                     font-weight:bold;
                                                     border-bottom:1px solid #e5e7eb;">
                                              Submitted At
                                          </td>

                                          <td style="padding:12px;
                                                     border-bottom:1px solid #e5e7eb;">
                                              %s
                                          </td>
                                      </tr>

                                  </table>

                                  <h3 style="margin-top:35px;color:#1f2937;">
                                      User Suggestion
                                  </h3>

                                  <div style="background:#f8fafc;
                                              border-left:5px solid #2563eb;
                                              padding:18px;
                                              border-radius:6px;
                                              line-height:1.7;
                                              color:#374151;">

                                      %s

                                  </div>

                                  <div style="margin-top:35px;text-align:center;">
                                                              <a href="%s"
                                     style="
                                         display:inline-block;
                                         background:#16a34a;
                                         color:#ffffff;
                                         text-decoration:none;
                                         padding:14px 28px;
                                         border-radius:6px;
                                         font-weight:bold;
                                         margin-right:12px;">
                                      ✅ Approve Feedback
                                  </a>

                                  <a href="%s"
                                     style="
                                         display:inline-block;
                                         background:#dc2626;
                                         color:#ffffff;
                                         text-decoration:none;
                                         padding:14px 28px;
                                         border-radius:6px;
                                         font-weight:bold;">
                                      ❌ Reject Feedback
                                  </a>

                              </div>

                              <hr style="
                                  margin:35px 0 20px 0;
                                  border:none;
                                  border-top:1px solid #e5e7eb;">

                              <p style="
                                  font-size:13px;
                                  color:#6b7280;
                                  line-height:1.7;
                                  text-align:center;">

                                  This feedback is currently in
                                  <strong>PENDING</strong> status.

                                  <br><br>

                                  Click
                                  <strong>Approve</strong>
                                  to publish this feedback on the website.

                                  <br><br>

                                  Click
                                  <strong>Reject</strong>
                                  to permanently delete this feedback.

                              </p>

                          </div>

                          <div style="
                              background:#f3f4f6;
                              padding:18px;
                              text-align:center;
                              font-size:12px;
                              color:#6b7280;">

                              AI Interview & Placement Preparation Platform

                              <br>

                              Automated Feedback Moderation Email

                          </div>

                      </div>

                      </body>
                      </html>
                      """;

            html = html.formatted(
                    feedback.getUser().getUsername(),
                    feedback.getUser().getEmail(),
                    String.valueOf(feedback.getRating()),
                    feedback.getCreatedAt().format(formatter),
                    suggestion,
                    approveUrl,
                    rejectUrl);

            helper.setText(html, true);

            mailSender.send(message);

        } catch (MessagingException e) {

            throw new RuntimeException(
                    "Failed to send feedback moderation email.",
                    e);
        }
    }
}