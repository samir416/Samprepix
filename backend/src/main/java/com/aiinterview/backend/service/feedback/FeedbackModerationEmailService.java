package com.aiinterview.backend.service.feedback;

import com.aiinterview.backend.entity.FeedbackApprovalToken;
import com.aiinterview.backend.entity.InterviewFeedback;

public interface FeedbackModerationEmailService {

    /**
     * Send feedback moderation email to the platform owner.
     *
     * @param feedback Submitted feedback
     * @param token Approval token
     */
    void sendFeedbackForApproval(
            InterviewFeedback feedback,
            FeedbackApprovalToken token
    );

}