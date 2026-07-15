package com.aiinterview.backend.service.interview;

import com.aiinterview.backend.dto.interview.InterviewQuestionRequest;
import com.aiinterview.backend.dto.interview.InterviewQuestionResponse;
import com.aiinterview.backend.dto.interview.StartInterviewRequest;
import com.aiinterview.backend.dto.interview.StartInterviewResponse;
import com.aiinterview.backend.entity.InterviewSession;
import com.aiinterview.backend.entity.User;
import com.aiinterview.backend.repository.InterviewSessionRepository;
import com.aiinterview.backend.service.gemini.GeminiService;
import org.springframework.stereotype.Service;

@Service
public class InterviewServiceImpl implements InterviewService {

    private final InterviewSessionRepository interviewSessionRepository;
    private final GeminiService geminiService;

    public InterviewServiceImpl(
            InterviewSessionRepository interviewSessionRepository,
            GeminiService geminiService) {

        this.interviewSessionRepository = interviewSessionRepository;
        this.geminiService = geminiService;
    }

    @Override
    public StartInterviewResponse startInterview(
            User user,
            StartInterviewRequest request) {

        String firstQuestion = geminiService.generateQuestion(
                request.getInterviewType(),
                1,
                request.getTotalQuestions()
        );

        InterviewSession session = InterviewSession.builder()
                .user(user)
                .interviewType(request.getInterviewType())
                .totalQuestions(request.getTotalQuestions())
                .currentQuestion(1)
                .currentQuestionText(firstQuestion)
                .score(0)
                .status("IN_PROGRESS")
                .build();

        session = interviewSessionRepository.save(session);

        return new StartInterviewResponse(
                session.getId(),
                session.getStatus(),
                "Interview started successfully.",
                firstQuestion,
                session.getStartedAt()
        );
    }
@Override
public InterviewQuestionResponse submitAnswer(
        User user,
        InterviewQuestionRequest request) {

    InterviewSession session = interviewSessionRepository
            .findByIdAndUser(request.getSessionId(), user)
            .orElseThrow(() -> new RuntimeException("Interview session not found."));

    String evaluation = geminiService.evaluateAnswer(
            session.getCurrentQuestionText(),
            request.getAnswer()
    );

    int score = geminiService.extractScore(evaluation);

    session.setScore(session.getScore() + score);

    int nextQuestionNo = session.getCurrentQuestion() + 1;

    if (nextQuestionNo > session.getTotalQuestions()) {

        session.setStatus("COMPLETED");
        session.setCompletedAt(java.time.LocalDateTime.now());

        interviewSessionRepository.save(session);

        return new InterviewQuestionResponse(
                evaluation,
                null,
                session.getCurrentQuestion(),
                session.getTotalQuestions(),
                session.getScore(),
                true
        );
    }

    String nextQuestion = geminiService.generateQuestion(
            session.getInterviewType(),
            nextQuestionNo,
            session.getTotalQuestions()
    );

    session.setCurrentQuestion(nextQuestionNo);
    session.setCurrentQuestionText(nextQuestion);

    interviewSessionRepository.save(session);

    return new InterviewQuestionResponse(
            evaluation,
            nextQuestion,
            session.getCurrentQuestion(),
            session.getTotalQuestions(),
            session.getScore(),
            false
    );
}
}