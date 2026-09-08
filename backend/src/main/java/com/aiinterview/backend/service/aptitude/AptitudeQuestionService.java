package com.aiinterview.backend.service.aptitude;

import com.aiinterview.backend.dto.aptitude.*;
import com.aiinterview.backend.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface AptitudeQuestionService {

    void seedQuestionBankIfNotPresent();

    Page<AptitudeQuestionDto> getQuestionsByTopic(String topicId, Pageable pageable);

    Page<AptitudeQuestionDto> getQuestionsByCategory(String category, String difficulty, Pageable pageable);

    CheckAnswerResponse checkAnswer(CheckAnswerRequest request);

    AptitudeStatsDto getAptitudeStats();

    AptitudeQuestionDto getQuestionById(Long id);

    List<AptitudeAssessmentQuestionDto> generateAssessment(String trackId, int count);

    AptitudeAttemptResponse submitAssessment(User user, AptitudeSubmitRequest request);

    List<AptitudeAttemptResponse> getUserAttempts(User user);

    AptitudeAttemptResponse getAttemptById(User user, Long attemptId);
}
