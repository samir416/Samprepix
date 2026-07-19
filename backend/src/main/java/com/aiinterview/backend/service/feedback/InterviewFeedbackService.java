package com.aiinterview.backend.service.feedback;

import com.aiinterview.backend.dto.feedback.FeedbackRequest;
import com.aiinterview.backend.dto.feedback.FeedbackResponse;
import com.aiinterview.backend.entity.InterviewFeedback;
import com.aiinterview.backend.entity.User;
import org.springframework.data.domain.Page;

public interface InterviewFeedbackService {

    /**
     * Submit feedback for an interview session.
     *
     * @param user Authenticated user
     * @param request Feedback request
     * @return Feedback response
     */
    FeedbackResponse submitFeedback(
            User user,
            FeedbackRequest request
    );

    /**
     * Get approved feedback for public display.
     *
     * @param page Page number
     * @param size Page size
     * @return Approved feedback list
     */
    Page<InterviewFeedback> getApprovedFeedback(
            int page,
            int size
    );

}