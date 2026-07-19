package com.aiinterview.backend.service.feedback;

import com.aiinterview.backend.entity.FeedbackApprovalToken;
import com.aiinterview.backend.entity.InterviewFeedback;
import com.aiinterview.backend.exception.InvalidTokenException;
import com.aiinterview.backend.exception.TokenAlreadyUsedException;
import com.aiinterview.backend.repository.FeedbackApprovalTokenRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@Transactional
public class FeedbackApprovalTokenServiceImpl
        implements FeedbackApprovalTokenService {

    private final FeedbackApprovalTokenRepository tokenRepository;

    public FeedbackApprovalTokenServiceImpl(
            FeedbackApprovalTokenRepository tokenRepository) {

        this.tokenRepository = tokenRepository;
    }

    @Override
    public FeedbackApprovalToken createToken(
            InterviewFeedback feedback) {

        if (tokenRepository.existsByFeedbackId(feedback.getId())) {
            throw new IllegalStateException(
                    "Approval token already exists for this feedback."
            );
        }

        FeedbackApprovalToken approvalToken =
                FeedbackApprovalToken.builder()
                        .feedback(feedback)
                        .token(UUID.randomUUID().toString())
                        .build();

        return tokenRepository.save(approvalToken);
    }

    @Override
    public FeedbackApprovalToken validateToken(
            String token) {

        FeedbackApprovalToken approvalToken =
                tokenRepository.findByToken(token)
                        .orElseThrow(() ->
                                new InvalidTokenException(
                                        "Invalid approval token."
                                ));

        if (approvalToken.isUsed()) {
            throw new TokenAlreadyUsedException(
                    "This approval link has already been used."
            );
        }

        return approvalToken;
    }

    @Override
    public void markAsUsed(
            FeedbackApprovalToken token) {

        token.setUsed(true);
        tokenRepository.save(token);
    }

    @Override
    public void deleteToken(
            FeedbackApprovalToken token) {

        tokenRepository.delete(token);
    }

    @Override
    public FeedbackApprovalToken getTokenByFeedback(
            Long feedbackId) {

        return tokenRepository.findByFeedbackId(feedbackId)
                .orElseThrow(() ->
                        new InvalidTokenException(
                                "Approval token not found."
                        ));
    }
}