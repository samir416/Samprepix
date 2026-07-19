package com.aiinterview.backend.service.feedback;

import com.aiinterview.backend.dto.feedback.FeedbackRequest;
import com.aiinterview.backend.dto.feedback.FeedbackResponse;
import com.aiinterview.backend.entity.FeedbackApprovalToken;
import com.aiinterview.backend.entity.FeedbackStatus;
import com.aiinterview.backend.entity.InterviewFeedback;
import com.aiinterview.backend.entity.InterviewSession;
import com.aiinterview.backend.entity.User;
import com.aiinterview.backend.repository.InterviewFeedbackRepository;
import com.aiinterview.backend.repository.InterviewSessionRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class InterviewFeedbackServiceImpl implements InterviewFeedbackService {

    private final InterviewFeedbackRepository interviewFeedbackRepository;
    private final InterviewSessionRepository interviewSessionRepository;
    private final FeedbackApprovalTokenService tokenService;
    private final FeedbackModerationEmailService emailService;

    public InterviewFeedbackServiceImpl(
            InterviewFeedbackRepository interviewFeedbackRepository,
            InterviewSessionRepository interviewSessionRepository,
            FeedbackApprovalTokenService tokenService,
            FeedbackModerationEmailService emailService) {

        this.interviewFeedbackRepository = interviewFeedbackRepository;
        this.interviewSessionRepository = interviewSessionRepository;
        this.tokenService = tokenService;
        this.emailService = emailService;
    }

    @Override
    public FeedbackResponse submitFeedback(
            User user,
            FeedbackRequest request) {

        // Find Interview Session
        InterviewSession session = interviewSessionRepository
                .findByIdAndUser(request.getSessionId(), user)
                .orElseThrow(() ->
                        new RuntimeException("Interview session not found."));

        // Prevent duplicate feedback
        if (interviewFeedbackRepository.existsByInterviewSession(session)) {

            return FeedbackResponse.builder()
                    .success(false)
                    .message("Feedback has already been submitted for this interview.")
                    .build();
        }

        // Save feedback
        InterviewFeedback feedback = InterviewFeedback.builder()
                .user(user)
                .interviewSession(session)
                .rating(request.getRating())
                .suggestion(request.getSuggestion())
                .status(FeedbackStatus.PENDING)
                .build();

        InterviewFeedback savedFeedback =
                interviewFeedbackRepository.save(feedback);

        // Create approval token
        FeedbackApprovalToken token =
                tokenService.createToken(savedFeedback);

        // Send moderation email
        emailService.sendFeedbackForApproval(
                savedFeedback,
                token
        );

        return FeedbackResponse.builder()
                .success(true)
                .message("Thank you for your feedback. It is pending approval.")
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public Page<InterviewFeedback> getApprovedFeedback(
            int page,
            int size) {

        return interviewFeedbackRepository
                .findByStatusOrderByCreatedAtDesc(
                        FeedbackStatus.APPROVED,
                        PageRequest.of(page, size)
                );
    }
}