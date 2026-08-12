package com.aiinterview.backend.service.interview;

import com.aiinterview.backend.dto.interview.*;
import com.aiinterview.backend.entity.InterviewAnswer;
import com.aiinterview.backend.entity.InterviewSession;
import com.aiinterview.backend.entity.User;
import com.aiinterview.backend.repository.InterviewAnswerRepository;
import com.aiinterview.backend.repository.InterviewSessionRepository;
import com.aiinterview.backend.service.gemini.GeminiService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class InterviewServiceImpl implements InterviewService {

        private final InterviewSessionRepository interviewSessionRepository;

        private final InterviewAnswerRepository interviewAnswerRepository;

        private final GeminiService geminiService;

        private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

        public InterviewServiceImpl(

                        InterviewSessionRepository interviewSessionRepository,

                        InterviewAnswerRepository interviewAnswerRepository,

                        GeminiService geminiService

        ) {

                this.interviewSessionRepository = interviewSessionRepository;

                this.interviewAnswerRepository = interviewAnswerRepository;

                this.geminiService = geminiService;

        }

        @Override
        public StartInterviewResponse startInterview(

                        User user,

                        StartInterviewRequest request

        ) {

                List<String> skills = request.getSkills();

                List<String> weakAreas = new ArrayList<>();

                List<String> strongAreas = new ArrayList<>();

                String firstQuestion = geminiService.generateQuestion(

                                request.getTargetRole(),

                                skills,

                                request.getExperienceLevel(),

                                List.of(),

                                List.of(),

                                weakAreas,

                                strongAreas

                );

                InterviewSession session = InterviewSession.builder()

                                .user(user)

                                .interviewType(
                                                request.getInterviewType())

                                .targetRole(
                                                request.getTargetRole())

                                .experienceLevel(
                                                request.getExperienceLevel())

                                .selectedSkills(
                                                writeJson(skills))

                                .weakAreas(
                                                writeJson(weakAreas))

                                .strongAreas(
                                                writeJson(strongAreas))

                                .currentQuestion(
                                                firstQuestion)

                                .previousQuestions(
                                                writeJson(new ArrayList<>()))

                                .previousAnswers(
                                                writeJson(new ArrayList<>()))

                                .questionsAnswered(0)

                                .overallScore(0)

                                .technicalAccuracy(0)

                                .completeness(0)

                                .communication(0)

                                .status("IN_PROGRESS")

                                .build();

                session = interviewSessionRepository.save(session);

                return StartInterviewResponse.builder()

                                .sessionId(
                                                session.getId())

                                .status(
                                                session.getStatus())

                                .question(

                                                firstQuestion

                                )

                                .startedAt(

                                                session.getStartedAt()

                                )

                                .build();

        }

        @Override
        public InterviewQuestionResponse submitAnswer(

                        User user,

                        InterviewQuestionRequest request

        ) {

                InterviewSession session = interviewSessionRepository
                                .findByIdAndUser(
                                                request.getSessionId(),
                                                user)
                                .orElseThrow(() -> new RuntimeException(
                                                "Interview session not found."));

                if ("COMPLETED".equalsIgnoreCase(session.getStatus())) {

                        throw new RuntimeException(
                                        "Interview session is already completed.");

                }

                List<String> skills = readJson(
                                session.getSelectedSkills());

                List<String> previousQuestions = readJson(
                                session.getPreviousQuestions());

                List<String> previousAnswers = readJson(
                                session.getPreviousAnswers());

                List<String> weakAreas = readJson(
                                session.getWeakAreas());

                List<String> strongAreas = readJson(
                                session.getStrongAreas());

                String evaluationJson = geminiService.evaluateAnswer(

                                session.getTargetRole(),

                                session.getExperienceLevel(),

                                skills,

                                session.getCurrentQuestion(),

                                request.getAnswer(),

                                previousQuestions,

                                previousAnswers,

                                ""

                );

                InterviewEvaluationResponse evaluation = readEvaluation(evaluationJson);

                InterviewAnswer answer = InterviewAnswer.builder()

                                .session(session)

                                .questionNumber(
                                                session.getQuestionsAnswered() + 1)

                                .question(
                                                session.getCurrentQuestion())

                                .answer(
                                                request.getAnswer())

                                .technicalAccuracy(
                                                evaluation.getTechnicalAccuracy())

                                .completeness(
                                                evaluation.getCompleteness())

                                .communication(
                                                evaluation.getCommunication())

                                .overallScore(
                                                evaluation.getOverallScore())

                                .performance(
                                                evaluation.getPerformance())

                                .difficulty(
                                                evaluation.getDifficulty())

                                .nextFocusSkill(
                                                evaluation.getNextFocusSkill())

                                .idealAnswer(
                                                evaluation.getIdealAnswer())

                                .feedback(
                                                evaluation.getFeedback())

                                .strengths(
                                                writeJson(
                                                                evaluation.getStrengths()))

                                .missingConcepts(
                                                writeJson(
                                                                evaluation.getMissingConcepts()))

                                .build();

                interviewAnswerRepository.save(answer);

                previousQuestions.add(
                                session.getCurrentQuestion());

                previousAnswers.add(
                                request.getAnswer());

                if (

                evaluation.getNextFocusSkill() != null &&

                                !evaluation.getNextFocusSkill().isBlank()

                ) {

                        if (!weakAreas.contains(
                                        evaluation.getNextFocusSkill())) {

                                weakAreas.add(
                                                evaluation.getNextFocusSkill());

                        }

                }

                String nextQuestion = geminiService.generateQuestion(

                                session.getTargetRole(),

                                skills,

                                session.getExperienceLevel(),

                                previousQuestions,

                                previousAnswers,

                                weakAreas,

                                strongAreas

                );

                session.setQuestionsAnswered(

                                session.getQuestionsAnswered() + 1

                );

                session.setCurrentQuestion(

                                nextQuestion

                );

                session.setPreviousQuestions(

                                writeJson(previousQuestions)

                );

                session.setPreviousAnswers(

                                writeJson(previousAnswers)

                );

                session.setWeakAreas(

                                writeJson(weakAreas)

                );

                session.setTechnicalAccuracy(

                                evaluation.getTechnicalAccuracy()

                );

                session.setCompleteness(

                                evaluation.getCompleteness()

                );

                session.setCommunication(

                                evaluation.getCommunication()

                );

                session.setOverallScore(

                                evaluation.getOverallScore()

                );

                session.setDifficulty(

                                evaluation.getDifficulty()

                );

                session.setNextFocusSkill(

                                evaluation.getNextFocusSkill()

                );

                interviewSessionRepository.save(

                                session

                );

                return InterviewQuestionResponse.builder()

                                .evaluation(

                                                evaluation.getFeedback()

                                )

                                .nextQuestion(

                                                nextQuestion

                                )

                                .questionNumber(

                                                session.getQuestionsAnswered() + 1

                                )

                                .score(

                                                evaluation.getOverallScore()

                                )

                                .technicalAccuracy(

                                                evaluation.getTechnicalAccuracy()

                                )

                                .completeness(

                                                evaluation.getCompleteness()

                                )

                                .communication(

                                                evaluation.getCommunication()

                                )

                                .interviewCompleted(

                                                false

                                )

                                .build();

        }

        @Override
        public InterviewResultResponse getInterviewResult(

                        User user,

                        Long sessionId

        ) {

                InterviewSession session =

                                interviewSessionRepository

                                                .findByIdAndUser(

                                                                sessionId,

                                                                user

                                                )

                                                .orElseThrow(() ->

                                                new RuntimeException(

                                                                "Interview session not found."

                                                )

                                                );

                List<InterviewAnswer> answers =

                                interviewAnswerRepository

                                                .findBySessionIdOrderByQuestionNumberAsc(

                                                                sessionId

                                                );

                List<InterviewAnswerResponse> answerResponses =

                                new ArrayList<>();

                for (

                InterviewAnswer answer : answers

                ) {

                        answerResponses.add(

                                        InterviewAnswerResponse.builder()

                                                        .questionNumber(

                                                                        answer.getQuestionNumber()

                                                        )

                                                        .question(

                                                                        answer.getQuestion()

                                                        )

                                                        .answer(

                                                                        answer.getAnswer()

                                                        )

                                                        .technicalAccuracy(

                                                                        answer.getTechnicalAccuracy()

                                                        )

                                                        .completeness(

                                                                        answer.getCompleteness()

                                                        )

                                                        .communication(

                                                                        answer.getCommunication()

                                                        )

                                                        .overallScore(

                                                                        answer.getOverallScore()

                                                        )

                                                        .performance(

                                                                        answer.getPerformance()

                                                        )

                                                        .difficulty(

                                                                        answer.getDifficulty()

                                                        )

                                                        .idealAnswer(

                                                                        answer.getIdealAnswer()

                                                        )

                                                        .feedback(

                                                                        answer.getFeedback()

                                                        )

                                                        .strengths(

                                                                        readJson(

                                                                                        answer.getStrengths())

                                                        )

                                                        .missingConcepts(

                                                                        readJson(

                                                                                        answer.getMissingConcepts())

                                                        )

                                                        .build()

                        );

                }

                int overallScore = 0;

                int technicalAccuracy = 0;

                int completeness = 0;

                int communication = 0;

                if (!answers.isEmpty()) {

                        overallScore = (int) Math.round(

                                        answers.stream()

                                                        .mapToInt(answer ->

                                                        answer.getOverallScore() == null

                                                                        ? 0

                                                                        : answer.getOverallScore())

                                                        .average()

                                                        .orElse(0)

                        );

                        technicalAccuracy = (int) Math.round(

                                        answers.stream()

                                                        .mapToInt(answer ->

                                                        answer.getTechnicalAccuracy() == null

                                                                        ? 0

                                                                        : answer.getTechnicalAccuracy())

                                                        .average()

                                                        .orElse(0)

                        );

                        completeness = (int) Math.round(

                                        answers.stream()

                                                        .mapToInt(answer ->

                                                        answer.getCompleteness() == null

                                                                        ? 0

                                                                        : answer.getCompleteness())

                                                        .average()

                                                        .orElse(0)

                        );

                        communication = (int) Math.round(

                                        answers.stream()

                                                        .mapToInt(answer ->

                                                        answer.getCommunication() == null

                                                                        ? 0

                                                                        : answer.getCommunication())

                                                        .average()

                                                        .orElse(0)

                        );

                }

                session.setOverallScore(overallScore);

                session.setTechnicalAccuracy(technicalAccuracy);

                session.setCompleteness(completeness);

                session.setCommunication(communication);

                if (!answers.isEmpty()) {

                        InterviewAnswer lastAnswer =

                                        answers.get(answers.size() - 1);

                        if (

                        lastAnswer.getNextFocusSkill() != null &&

                                        !lastAnswer.getNextFocusSkill().isBlank()

                        ) {

                                session.setNextFocusSkill(

                                                lastAnswer.getNextFocusSkill()

                                );

                        }

                        if (

                        lastAnswer.getDifficulty() != null &&

                                        !lastAnswer.getDifficulty().isBlank()

                        ) {

                                session.setDifficulty(

                                                lastAnswer.getDifficulty()

                                );

                        }

                }

                interviewSessionRepository.save(session);

                return InterviewResultResponse.builder()

                                .sessionId(

                                                session.getId())

                                .status(

                                                session.getStatus())

                                .targetRole(

                                                session.getTargetRole())

                                .experienceLevel(

                                                session.getExperienceLevel())

                                .skills(

                                                readJson(session.getSelectedSkills()))

                                .questionsAnswered(

                                                answers.size())

                                .overallScore(

                                                overallScore)

                                .technicalAccuracy(

                                                technicalAccuracy)

                                .completeness(

                                                completeness)

                                .communication(

                                                communication)

                                .nextFocusSkill(

                                                session.getNextFocusSkill())

                                .difficulty(
                                                session.getDifficulty())
                                .startedAt(
                                                session.getStartedAt())
                                .completedAt(
                                                session.getCompletedAt())
                                .answers(
                                                answerResponses)
                                .build();

        }

        @Override
public long getCompletedInterviewCount(
        User user
) {

    return interviewSessionRepository
            .countByUserAndStatus(
                    user,
                    "COMPLETED"
            );
}

        @Override
        public InterviewProgressResponse getInterviewProgress(

                        User user,

                        Long sessionId

        ) {

                InterviewSession session =

                                interviewSessionRepository

                                                .findByIdAndUser(

                                                                sessionId,

                                                                user

                                                )

                                                .orElseThrow(() ->

                                                new RuntimeException(

                                                                "Interview session not found."

                                                )

                                                );

                return InterviewProgressResponse.builder()

                                .sessionId(

                                                session.getId()

                                )

                                .status(

                                                session.getStatus()

                                )

                                .questionsAnswered(

                                                session.getQuestionsAnswered()

                                )

                                .overallScore(

                                                session.getOverallScore()

                                )

                                .technicalAccuracy(

                                                session.getTechnicalAccuracy()

                                )

                                .completeness(

                                                session.getCompleteness()

                                )

                                .communication(

                                                session.getCommunication()

                                )

                                .currentQuestion(

                                                session.getCurrentQuestion()

                                )

                                .targetRole(

                                                session.getTargetRole()

                                )

                                .experienceLevel(

                                                session.getExperienceLevel()

                                )

                                .skills(

                                                readJson(session.getSelectedSkills())

                                )

                                .interviewEndedByUser(

                                                session.getInterviewEndedByUser()

                                )

                                .reportGenerated(

                                                session.getReportGenerated()

                                )

                                .startedAt(

                                                session.getStartedAt()

                                )

                                .completedAt(

                                                session.getCompletedAt()

                                )

                                .build();

        }

        private List<String> readJson(

                        String json

        ) {

                try {

                        if (

                        json == null ||

                                        json.isBlank()

                        ) {

                                return new ArrayList<>();

                        }

                        return OBJECT_MAPPER.readValue(

                                        json,

                                        new TypeReference<List<String>>() {
                                        }

                        );

                }

                catch (Exception exception) {

                        return new ArrayList<>();

                }

        }

        private String writeJson(

                        List<String> list

        ) {

                try {

                        if (

                        list == null

                        ) {

                                list = new ArrayList<>();

                        }

                        return OBJECT_MAPPER.writeValueAsString(

                                        list

                        );

                }

                catch (Exception exception) {

                        return "[]";

                }

        }

        @Override
        public void endInterview(

                        Long sessionId,

                        User user

        ) {

                InterviewSession session = interviewSessionRepository

                                .findByIdAndUser(

                                                sessionId,

                                                user

                                )

                                .orElseThrow(() ->

                                new RuntimeException(

                                                "Interview session not found."

                                )

                                );

                List<InterviewAnswer> answers =

                                interviewAnswerRepository

                                                .findBySessionIdOrderByQuestionNumberAsc(

                                                                sessionId

                                                );

                int overallScore = 0;

                int technicalAccuracy = 0;

                int completeness = 0;

                int communication = 0;

                if (!answers.isEmpty()) {

                        overallScore = (int) Math.round(

                                        answers.stream()

                                                        .mapToInt(answer ->

                                                        answer.getOverallScore() == null

                                                                        ? 0

                                                                        : answer.getOverallScore())

                                                        .average()

                                                        .orElse(0)

                        );

                        technicalAccuracy = (int) Math.round(

                                        answers.stream()

                                                        .mapToInt(answer ->

                                                        answer.getTechnicalAccuracy() == null

                                                                        ? 0

                                                                        : answer.getTechnicalAccuracy())

                                                        .average()

                                                        .orElse(0)

                        );

                        completeness = (int) Math.round(

                                        answers.stream()

                                                        .mapToInt(answer ->

                                                        answer.getCompleteness() == null

                                                                        ? 0

                                                                        : answer.getCompleteness())

                                                        .average()

                                                        .orElse(0)

                        );

                        communication = (int) Math.round(

                                        answers.stream()

                                                        .mapToInt(answer ->

                                                        answer.getCommunication() == null

                                                                        ? 0

                                                                        : answer.getCommunication())

                                                        .average()

                                                        .orElse(0)

                        );

                        InterviewAnswer lastAnswer =

                                        answers.get(answers.size() - 1);

                        if (

                        lastAnswer.getNextFocusSkill() != null &&

                                        !lastAnswer.getNextFocusSkill().isBlank()

                        ) {

                                session.setNextFocusSkill(

                                                lastAnswer.getNextFocusSkill()

                                );

                        }

                        if (

                        lastAnswer.getDifficulty() != null &&

                                        !lastAnswer.getDifficulty().isBlank()

                        ) {

                                session.setDifficulty(

                                                lastAnswer.getDifficulty()

                                );

                        }

                }

                session.setOverallScore(

                                overallScore

                );

                session.setTechnicalAccuracy(

                                technicalAccuracy

                );

                session.setCompleteness(

                                completeness

                );

                session.setCommunication(

                                communication

                );

                session.setQuestionsAnswered(

                                answers.size()

                );

                session.setInterviewEndedByUser(

                                true

                );

                session.setStatus(

                                "COMPLETED"

                );

                session.setReportGenerated(

                                true

                );

                session.setCompletedAt(

                                LocalDateTime.now()

                );

                interviewSessionRepository.save(

                                session

                );

        }

        private InterviewEvaluationResponse readEvaluation(

                        String json

        ) {

                try {

                        if (

                        json == null ||

                                        json.isBlank()

                        ) {

                                throw new RuntimeException(

                                                "Empty Gemini evaluation response."

                                );

                        }

                        return OBJECT_MAPPER.readValue(

                                        json,

                                        InterviewEvaluationResponse.class

                        );

                }

                catch (Exception exception) {

                        throw new RuntimeException(

                                        "Failed to parse Gemini evaluation response.",

                                        exception

                        );

                }

        }

}
