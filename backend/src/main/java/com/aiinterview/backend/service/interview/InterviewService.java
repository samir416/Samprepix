package com.aiinterview.backend.service.interview;

import com.aiinterview.backend.dto.interview.StartInterviewRequest;
import com.aiinterview.backend.dto.interview.StartInterviewResponse;
import com.aiinterview.backend.entity.User;

public interface InterviewService {

    StartInterviewResponse startInterview(User user, StartInterviewRequest request);

}