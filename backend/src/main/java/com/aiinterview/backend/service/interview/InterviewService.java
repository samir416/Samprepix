package com.aiinterview.backend.service.interview;

import com.aiinterview.backend.dto.interview.InterviewQuestionRequest;
import com.aiinterview.backend.dto.interview.InterviewQuestionResponse;
import com.aiinterview.backend.dto.interview.InterviewResultResponse;
import com.aiinterview.backend.dto.interview.StartInterviewRequest;
import com.aiinterview.backend.dto.interview.StartInterviewResponse;
import com.aiinterview.backend.entity.User;
import com.aiinterview.backend.dto.interview.InterviewProgressResponse;

public interface InterviewService {

    StartInterviewResponse startInterview(
            User user,
            StartInterviewRequest request
    );

    InterviewQuestionResponse submitAnswer(
            User user,
            InterviewQuestionRequest request
    );

    InterviewResultResponse getInterviewResult(
            User user,
            Long sessionId
    );

    InterviewProgressResponse getInterviewProgress(
        User user,
        Long sessionId
);
}