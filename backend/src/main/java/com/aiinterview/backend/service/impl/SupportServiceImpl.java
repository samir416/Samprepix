package com.aiinterview.backend.service.impl;

import com.aiinterview.backend.dto.support.ReportProblemRequest;
import com.aiinterview.backend.dto.support.SupportQuestionResponse;
import com.aiinterview.backend.entity.User;
import com.aiinterview.backend.repository.UserRepository;
import com.aiinterview.backend.service.EmailService;
import com.aiinterview.backend.service.SupportService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Locale;
import java.util.Optional;

@Service
public class SupportServiceImpl implements SupportService {

    private static final Logger log = LoggerFactory.getLogger(SupportServiceImpl.class);

    private final EmailService emailService;
    private final UserRepository userRepository;

    @Value("${feedback.moderation.owner-email:support@samprepix.com}")
    private String adminOwnerEmail;

    public SupportServiceImpl(EmailService emailService, UserRepository userRepository) {
        this.emailService = emailService;
        this.userRepository = userRepository;
    }

    @Override
    public void submitProblemReport(ReportProblemRequest request, String authenticatedUsername) {
        if (request == null) {
            throw new IllegalArgumentException("Report request cannot be empty.");
        }

        String feature = request.getFeature() != null && !request.getFeature().isBlank()
                ? request.getFeature().trim()
                : "General";

        String description = request.getDescription();
        if (description == null || description.trim().isBlank()) {
            throw new IllegalArgumentException("Problem description is required.");
        }
        if (description.length() > 3000) {
            description = description.substring(0, 3000);
        }

        String actionAttempted = request.getActionAttempted();
        if (actionAttempted != null && actionAttempted.length() > 1000) {
            actionAttempted = actionAttempted.substring(0, 1000);
        }

        String reporterEmail = request.getReporterEmail();
        String username = "Guest User";

        if (authenticatedUsername != null && !authenticatedUsername.isBlank()) {
            Optional<User> userOpt = userRepository.findByUsername(authenticatedUsername);
            if (userOpt.isPresent()) {
                User user = userOpt.get();
                username = user.getUsername();
                if (reporterEmail == null || reporterEmail.isBlank()) {
                    reporterEmail = user.getEmail();
                }
            } else {
                username = authenticatedUsername;
            }
        }

        if (reporterEmail == null || reporterEmail.isBlank()) {
            reporterEmail = "user-feedback@samprepix.com";
        }

        String targetAdmin = adminOwnerEmail != null && !adminOwnerEmail.isBlank()
                ? adminOwnerEmail.trim()
                : "support@samprepix.com";

        log.info("Dispatching problem report for feature '{}' by '{}' ({}) to admin '{}'",
                feature, username, reporterEmail, targetAdmin);

        try {
            emailService.sendProblemReportEmail(
                    targetAdmin,
                    reporterEmail,
                    username,
                    feature,
                    description,
                    actionAttempted,
                    request.getPageUrl()
            );
        } catch (Exception ex) {
            log.error("Failed to deliver problem report email. Logging report internally. Error: {}", ex.getMessage(), ex);
            // Fallback: don't fail user request if mail transport is offline in local test environment
        }
    }

    @Override
    public SupportQuestionResponse answerQuestion(String question) {
        if (question == null || question.trim().isBlank()) {
            return new SupportQuestionResponse(
                    "Hello! I am your Samprepix Support Assistant. You can ask me how to use the AI Roadmap, GitHub Profile Analyzer, Coding Arena, Mock Interviews, or subscription plans.",
                    "platform_kb"
            );
        }

        String q = question.toLowerCase(Locale.ROOT);

        if (q.contains("roadmap") || q.contains("ai roadmap") || q.contains("path") || q.contains("milestone")) {
            return new SupportQuestionResponse(
                    "**Samprepix AI Roadmap** generates a step-by-step personalized placement preparation curriculum based on your target role, current skills, and timeline. "
                            + "You can view milestones, mark topics as completed, track overall progress percentages, and access targeted study materials for each phase.",
                    "knowledge_base"
            );
        }

        if (q.contains("github") || q.contains("analyzer") || q.contains("profile") || q.contains("repo")) {
            return new SupportQuestionResponse(
                    "The **GitHub Profile Analyzer** evaluates your public repositories, languages, commit patterns, and code contributions. "
                            + "It generates an industry readiness score, identifies your top technical skills, and recommends enhancements to strengthen your engineering portfolio for recruiters.",
                    "knowledge_base"
            );
        }

        if (q.contains("coding") || q.contains("arena") || q.contains("language") || q.contains("compiler") || q.contains("piston") || q.contains("run")) {
            return new SupportQuestionResponse(
                    "The **Coding Arena** supports 8 industry programming languages: Java, Python, C++, C, JavaScript, TypeScript, Go, and Rust. "
                            + "You can write code with syntax highlighting, run code against custom inputs or hidden test cases, get real-time compiler diagnostics, and request AI hints when stuck.",
                    "knowledge_base"
            );
        }

        if (q.contains("pro") || q.contains("elite") || q.contains("price") || q.contains("pricing") || q.contains("subscription") || q.contains("plan")) {
            return new SupportQuestionResponse(
                    "**Samprepix Plans**:\n"
                            + "- **PRO (₹1 test price)**: Unlocks full Coding Arena access, AI Roadmap generator, Resume Analyzer, and core aptitude practice.\n"
                            + "- **ELITE (₹2 test price)**: Everything in Pro plus unlimited AI Mock Interviews with video/audio feedback, advanced performance telemetry, and priority AI hints.",
                    "knowledge_base"
            );
        }

        if (q.contains("mock") || q.contains("interview") || q.contains("voice") || q.contains("speech")) {
            return new SupportQuestionResponse(
                    "**AI Mock Interviews** simulate real technical and HR interviews with speech-to-text recognition, real-time AI follow-up questions, and comprehensive scoring on clarity, accuracy, and depth. Detailed evaluation reports are stored in your Performance Hub.",
                    "knowledge_base"
            );
        }

        if (q.contains("aptitude") || q.contains("mcq") || q.contains("test")) {
            return new SupportQuestionResponse(
                    "The **Aptitude Hub** contains curated placement MCQs covering Quantitative, Logical Reasoning, and Verbal ability with timed quizzes, detailed solutions, and accuracy metrics.",
                    "knowledge_base"
            );
        }

        if (q.contains("resume") || q.contains("ats")) {
            return new SupportQuestionResponse(
                    "The **Resume Analyzer** scans your PDF/DOCX resume against ATS algorithms, scoring structure, keywords, and relevance for software engineering roles with specific improvement suggestions.",
                    "knowledge_base"
            );
        }

        return new SupportQuestionResponse(
                "Samprepix is your comprehensive AI Placement & Interview Preparation Platform. "
                        + "You can explore the **Dashboard**, practice in the **Coding Arena**, simulate **Mock Interviews**, inspect your **GitHub Profile**, and generate your **AI Roadmap**. "
                        + "Need to report a bug? Click 'Report an Issue' in the assistant header.",
                "general_guidance"
        );
    }
}
