package com.aiinterview.backend.service.ai;

import com.aiinterview.backend.dto.ai.AIResponse;

public interface AIService {

AIResponse analyzeResume(String resumeText) throws Exception;
}