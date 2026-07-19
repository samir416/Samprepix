package com.aiinterview.backend.service.feedback;

import com.aiinterview.backend.entity.FeedbackApprovalToken;
import com.aiinterview.backend.entity.InterviewFeedback;

public interface FeedbackApprovalTokenService {

    /**
     * Generate and save a new approval token
     * for the given feedback.
     */
    FeedbackApprovalToken createToken(InterviewFeedback feedback);

    /**
     * Validate token.
     */
    FeedbackApprovalToken validateToken(String token);

    /**
     * Mark token as used after successful approval.
     */
    void markAsUsed(FeedbackApprovalToken token);

    /**
     * Remove token permanently.
     */
    void deleteToken(FeedbackApprovalToken token);

    /**
     * Find token for a feedback.
     */
    FeedbackApprovalToken getTokenByFeedback(Long feedbackId);
}