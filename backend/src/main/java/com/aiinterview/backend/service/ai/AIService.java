package com.aiinterview.backend.service.ai;

import com.aiinterview.backend.dto.ai.AIResponse;

public interface AIService {

    AIResponse analyzeResume(String resumeText) throws Exception;

    String generateCodingHint(
            String problemTitle,
            String problemDescription,
            String language,
            String code
    ) throws Exception;
}