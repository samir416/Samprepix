package com.aiinterview.backend.service;

import com.aiinterview.backend.dto.support.ReportProblemRequest;
import com.aiinterview.backend.dto.support.SupportQuestionResponse;

public interface SupportService {

    void submitProblemReport(ReportProblemRequest request, String authenticatedUsername);

    SupportQuestionResponse answerQuestion(String question);
}
