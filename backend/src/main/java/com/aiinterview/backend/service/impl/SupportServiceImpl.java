package com.aiinterview.backend.service.impl;

import com.aiinterview.backend.dto.groq.Message;
import com.aiinterview.backend.dto.support.ReportProblemRequest;
import com.aiinterview.backend.dto.support.SupportQuestionResponse;
import com.aiinterview.backend.entity.User;
import com.aiinterview.backend.repository.UserRepository;
import com.aiinterview.backend.service.EmailService;
import com.aiinterview.backend.service.SupportService;
import com.aiinterview.backend.service.ai.GroqService;
import com.aiinterview.backend.service.gemini.GeminiService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

@Service
public class SupportServiceImpl implements SupportService {

    private static final Logger log = LoggerFactory.getLogger(SupportServiceImpl.class);

    private final EmailService emailService;
    private final UserRepository userRepository;
    private final GroqService groqService;
    private final GeminiService geminiService;

    @Value("${feedback.moderation.owner-email:support@samprepix.com}")
    private String adminOwnerEmail;

    public SupportServiceImpl(
            EmailService emailService,
            UserRepository userRepository,
            GroqService groqService,
            GeminiService geminiService
    ) {
        this.emailService = emailService;
        this.userRepository = userRepository;
        this.groqService = groqService;
        this.geminiService = geminiService;
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
        return answerQuestion(question, null);
    }

    @Override
    public SupportQuestionResponse answerQuestion(String question, List<Map<String, String>> history) {
        if (question == null || question.trim().isBlank()) {
            return new SupportQuestionResponse(
                    "Hello! 👋 I'm your SamPrepIX AI Assistant. I can help guide you through technical interview prep, coding challenges, Spring Boot, Java, React, DSA, and your SamPrepIX placement tools. What would you like to prepare for today?",
                    "greeting"
            );
        }

        String trimmedQuestion = question.trim();

        // 1. Primary AI inference: Groq (openai/gpt-oss-120b) with multi-turn conversation context
        try {
            List<Message> groqMessages = new ArrayList<>();
            groqMessages.add(new Message("system",
                    "You are SamPrepIX AI Assistant, the official AI mentor, tutor, and platform guide for the SamPrepIX placement platform. "
                    + "You assist students and candidates with: "
                    + "1. Technical interview concepts and explanations across Java, Spring Boot, React, Python, C++, DSA, System Design, SQL, and Full Stack development. "
                    + "2. Writing clean code solutions, debugging, algorithms, and step-by-step explanations. "
                    + "3. Conducting technical and HR mock interviews when requested to act as an interviewer (ask exactly one focused question at a time and wait for the user's answer). "
                    + "4. Guidance on SamPrepIX features (AI Roadmap, Coding Arena, Mock Interviews, Aptitude Hub, Resume Analyzer, GitHub Profiler). "
                    + "Maintain conversational context across multi-turn interactions. Format all responses clearly and professionally using Markdown."
            ));

            if (history != null && !history.isEmpty()) {
                int startIdx = Math.max(0, history.size() - 8);
                for (int i = startIdx; i < history.size(); i++) {
                    Map<String, String> item = history.get(i);
                    if (item == null) continue;
                    String sender = item.get("sender");
                    if (sender == null) sender = item.get("role");
                    String text = item.get("text");
                    if (text == null) text = item.get("content");
                    if (text == null || text.isBlank()) continue;

                    String role = ("bot".equalsIgnoreCase(sender) || "assistant".equalsIgnoreCase(sender))
                            ? "assistant"
                            : "user";
                    groqMessages.add(new Message(role, text.trim()));
                }
            }

            groqMessages.add(new Message("user", trimmedQuestion));

            String aiResponse = groqService.generateChatResponse(groqMessages);
            if (aiResponse != null && !aiResponse.isBlank()) {
                return new SupportQuestionResponse(aiResponse, "ai_assistant");
            }
        } catch (Exception groqEx) {
            log.warn("Groq AI chat invocation failed, attempting Gemini fallback: {}", groqEx.getMessage());
        }

        // 2. Secondary AI fallback: Gemini (gemini-flash-latest)
        try {
            StringBuilder geminiPrompt = new StringBuilder();
            geminiPrompt.append("You are SamPrepIX AI Assistant, the official technical mentor and placement tutor for the SamPrepIX platform.\n");
            geminiPrompt.append("Provide a clear, accurate, professional response in Markdown.\n\n");

            if (history != null && !history.isEmpty()) {
                geminiPrompt.append("--- Conversation Context ---\n");
                int startIdx = Math.max(0, history.size() - 6);
                for (int i = startIdx; i < history.size(); i++) {
                    Map<String, String> item = history.get(i);
                    if (item == null) continue;
                    String sender = item.get("sender");
                    if (sender == null) sender = item.get("role");
                    String text = item.get("text");
                    if (text == null) text = item.get("content");
                    if (text == null || text.isBlank()) continue;

                    String role = ("bot".equalsIgnoreCase(sender) || "assistant".equalsIgnoreCase(sender))
                            ? "Assistant"
                            : "User";
                    geminiPrompt.append(role).append(": ").append(text.trim()).append("\n");
                }
                geminiPrompt.append("----------------------------\n\n");
            }

            geminiPrompt.append("User: ").append(trimmedQuestion).append("\nAssistant:");

            String geminiResponse = geminiService.generateChatResponse(geminiPrompt.toString());
            if (geminiResponse != null && !geminiResponse.isBlank()) {
                return new SupportQuestionResponse(geminiResponse, "ai_assistant");
            }
        } catch (Exception geminiEx) {
            log.error("Gemini AI chat fallback also failed: {}", geminiEx.getMessage());
        }

        // 3. User-friendly notice if network connection fails (NEVER expose raw exceptions or keys)
        return new SupportQuestionResponse(
                "I'm temporarily experiencing connectivity issues reaching the AI inference engine. Please retry in a few moments, or explore your preparation modules directly from the sidebar.",
                "service_notice"
        );
    }
}
