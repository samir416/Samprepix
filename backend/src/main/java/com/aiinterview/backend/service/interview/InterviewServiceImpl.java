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
        import com.aiinterview.backend.entity.InterviewAnswer;
        import com.aiinterview.backend.repository.InterviewAnswerRepository;
        import com.aiinterview.backend.dto.interview.InterviewResultResponse;
        import com.aiinterview.backend.dto.interview.InterviewAnswerResponse;
        import java.util.List;
        import java.util.stream.Collectors;
        import com.aiinterview.backend.dto.interview.InterviewProgressResponse;

        @Service
        public class InterviewServiceImpl implements InterviewService {

                private final InterviewSessionRepository interviewSessionRepository;
                private final GeminiService geminiService;
                private final InterviewAnswerRepository interviewAnswerRepository;

                public InterviewServiceImpl(
                                InterviewSessionRepository interviewSessionRepository,
                                InterviewAnswerRepository interviewAnswerRepository,
                                GeminiService geminiService) {

                        this.interviewSessionRepository = interviewSessionRepository;
                        this.interviewAnswerRepository = interviewAnswerRepository;
                        this.geminiService = geminiService;
                }

                @Override
                public StartInterviewResponse startInterview(
                                User user,
                                StartInterviewRequest request) {

                        String firstQuestion = geminiService.generateQuestion(
                                        request.getInterviewType(),
                                        1,
                                        request.getTotalQuestions());

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
                                        session.getStartedAt());
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
                                        request.getAnswer());

                        int score = geminiService.extractScore(evaluation);

                        InterviewAnswer interviewAnswer = InterviewAnswer.builder()
                                        .session(session)
                                        .questionNumber(session.getCurrentQuestion())
                                        .question(session.getCurrentQuestionText())
                                        .answer(request.getAnswer())
                                        .evaluation(evaluation)
                                        .score(score)
                                        .build();

                        interviewAnswerRepository.save(interviewAnswer);

                        session.setScore(session.getScore() + score);

                        int nextQuestionNo = session.getCurrentQuestion() + 1;

                        if (nextQuestionNo > session.getTotalQuestions()) {

                                session.setStatus("COMPLETED");
                                session.setCompletedAt(java.time.LocalDateTime.now());

                                double percentage = (session.getScore() * 100.0) /
                                                (session.getTotalQuestions() * 10.0);

                                session.setPercentage(percentage);

                                if (percentage >= 80) {

                                        session.setOverallFeedback("Excellent Performance");
                                        session.setStrengths("Strong communication, confidence and technical understanding.");
                                        session.setWeaknesses("Minor improvements can be made in answer depth.");
                                        session.setSuggestions("Keep practicing advanced interview questions.");

                                } else if (percentage >= 60) {

                                        session.setOverallFeedback("Good Performance");
                                        session.setStrengths("Good understanding of concepts.");
                                        session.setWeaknesses("Need better explanation and confidence.");
                                        session.setSuggestions("Practice more mock interviews and improve answer structure.");

                                } else {

                                        session.setOverallFeedback("Needs Improvement");
                                        session.setStrengths("Basic understanding present.");
                                        session.setWeaknesses("Communication and technical explanation need improvement.");
                                        session.setSuggestions("Practice fundamentals and attend more mock interviews.");

                                }

                                interviewSessionRepository.save(session);

                                return new InterviewQuestionResponse(
                                                evaluation,
                                                null,
                                                session.getCurrentQuestion(),
                                                session.getTotalQuestions(),
                                                session.getScore(),
                                                true);
                        }

                        String nextQuestion = geminiService.generateQuestion(
                                        session.getInterviewType(),
                                        nextQuestionNo,
                                        session.getTotalQuestions());

                        session.setCurrentQuestion(nextQuestionNo);
                        session.setCurrentQuestionText(nextQuestion);

                        interviewSessionRepository.save(session);

                        return new InterviewQuestionResponse(
                                        evaluation,
                                        nextQuestion,
                                        session.getCurrentQuestion(),
                                        session.getTotalQuestions(),
                                        session.getScore(),
                                        false);
                }

        @Override
        public InterviewResultResponse getInterviewResult(
                User user,
                Long sessionId) {

        InterviewSession session = interviewSessionRepository
                .findByIdAndUser(sessionId, user)
                .orElseThrow(() ->
                        new RuntimeException("Interview session not found."));

        List<InterviewAnswerResponse> answers =
                interviewAnswerRepository
                        .findBySessionIdOrderByQuestionNumberAsc(sessionId)
                        .stream()
                        .map(answer -> InterviewAnswerResponse.builder()
                                .questionNumber(answer.getQuestionNumber())
                                .question(answer.getQuestion())
                                .answer(answer.getAnswer())
                                .evaluation(answer.getEvaluation())
                                .score(answer.getScore())
                                .build())
                        .collect(Collectors.toList());

        return InterviewResultResponse.builder()
                .sessionId(session.getId())
                .status(session.getStatus())
                .interviewType(session.getInterviewType())
                .totalQuestions(session.getTotalQuestions())
                .score(session.getScore())
                .percentage(session.getPercentage())
                .overallFeedback(session.getOverallFeedback())
                .strengths(session.getStrengths())
                .weaknesses(session.getWeaknesses())
                .suggestions(session.getSuggestions())
                .answers(answers)
                .build();
        }

        @Override
        public InterviewProgressResponse getInterviewProgress(
                User user,
                Long sessionId) {

        InterviewSession session = interviewSessionRepository
                .findByIdAndUser(sessionId, user)
                .orElseThrow(() ->
                        new RuntimeException("Interview session not found."));

        int answeredQuestions = session.getCurrentQuestion() - 1;

        if ("COMPLETED".equals(session.getStatus())) {
                answeredQuestions = session.getTotalQuestions();
        }

        int remainingQuestions =
                session.getTotalQuestions() - answeredQuestions;

        return InterviewProgressResponse.builder()
                .sessionId(session.getId())
                .status(session.getStatus())
                .currentQuestion(session.getCurrentQuestion())
                .totalQuestions(session.getTotalQuestions())
                .answeredQuestions(answeredQuestions)
                .remainingQuestions(remainingQuestions)
                .score(session.getScore())
                .percentage(session.getPercentage())
                .build();
        }

        }