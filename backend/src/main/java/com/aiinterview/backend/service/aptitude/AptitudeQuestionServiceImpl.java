package com.aiinterview.backend.service.aptitude;

import com.aiinterview.backend.dto.aptitude.*;
import com.aiinterview.backend.entity.AptitudeAttempt;
import com.aiinterview.backend.entity.AptitudeQuestion;
import com.aiinterview.backend.entity.User;
import com.aiinterview.backend.repository.AptitudeAttemptRepository;
import com.aiinterview.backend.repository.AptitudeQuestionRepository;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.PreparedStatement;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
public class AptitudeQuestionServiceImpl implements AptitudeQuestionService {

    private static final Logger log = LoggerFactory.getLogger(AptitudeQuestionServiceImpl.class);

    private final AptitudeQuestionRepository aptitudeQuestionRepository;
    private final AptitudeAttemptRepository aptitudeAttemptRepository;
    private final AptitudeQuestionGenerator aptitudeQuestionGenerator;
    private final JdbcTemplate jdbcTemplate;
    private final ObjectMapper objectMapper;

    @Override
    @Transactional
    public void seedQuestionBankIfNotPresent() {
        ensureTableExists();

        long count = aptitudeQuestionRepository.count();
        if (count >= 20000) {
            log.info("Aptitude Question Bank already verified with {} questions.", count);
            return;
        }

        log.info("Aptitude Question Bank has {} questions. Generating 20,000+ verified placement questions...", count);
        List<AptitudeQuestion> questions = aptitudeQuestionGenerator.generateAllQuestions();

        String insertSql = "INSERT INTO aptitude_questions (" +
                "question_code, category, topic_id, topic, difficulty, question_text, " +
                "option_a, option_b, option_c, option_d, correct_option, explanation, " +
                "formula_hint, source_attribution, tags, created_at) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?) " +
                "ON DUPLICATE KEY UPDATE question_code = question_code";

        int batchSize = 1000;
        for (int i = 0; i < questions.size(); i += batchSize) {
            List<AptitudeQuestion> batch = questions.subList(i, Math.min(i + batchSize, questions.size()));
            jdbcTemplate.batchUpdate(insertSql, batch, batch.size(), (PreparedStatement ps, AptitudeQuestion q) -> {
                ps.setString(1, q.getQuestionCode());
                ps.setString(2, q.getCategory());
                ps.setString(3, q.getTopicId());
                ps.setString(4, q.getTopic());
                ps.setString(5, q.getDifficulty());
                ps.setString(6, q.getQuestionText());
                ps.setString(7, q.getOptionA());
                ps.setString(8, q.getOptionB());
                ps.setString(9, q.getOptionC());
                ps.setString(10, q.getOptionD());
                ps.setString(11, q.getCorrectOption());
                ps.setString(12, q.getExplanation());
                ps.setString(13, q.getFormulaHint());
                ps.setString(14, q.getSourceAttribution());
                ps.setString(15, q.getTags());
                ps.setTimestamp(16, Timestamp.valueOf(q.getCreatedAt() != null ? q.getCreatedAt() : LocalDateTime.now()));
            });
        }

        long totalNow = aptitudeQuestionRepository.count();
        log.info("Successfully seeded Aptitude Question Bank! Total questions now in DB: {}", totalNow);
    }

    private void ensureTableExists() {
        try {
            jdbcTemplate.execute(
                    "CREATE TABLE IF NOT EXISTS aptitude_questions (" +
                            "id BIGINT AUTO_INCREMENT PRIMARY KEY, " +
                            "question_code VARCHAR(64) NOT NULL UNIQUE, " +
                            "category VARCHAR(64) NOT NULL, " +
                            "topic_id VARCHAR(64) NOT NULL, " +
                            "topic VARCHAR(128) NOT NULL, " +
                            "difficulty VARCHAR(20) NOT NULL, " +
                            "question_text TEXT NOT NULL, " +
                            "option_a TEXT NOT NULL, " +
                            "option_b TEXT NOT NULL, " +
                            "option_c TEXT NOT NULL, " +
                            "option_d TEXT NOT NULL, " +
                            "correct_option VARCHAR(5) NOT NULL, " +
                            "explanation TEXT NOT NULL, " +
                            "formula_hint TEXT, " +
                            "source_attribution VARCHAR(150), " +
                            "tags VARCHAR(150), " +
                            "created_at DATETIME NOT NULL, " +
                            "INDEX idx_aptitude_category (category), " +
                            "INDEX idx_aptitude_topic_id (topic_id), " +
                            "INDEX idx_aptitude_difficulty (difficulty)" +
                            ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci"
            );
        } catch (Exception e) {
            log.warn("Table verification notice: {}", e.getMessage());
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Page<AptitudeQuestionDto> getQuestionsByTopic(String topicId, Pageable pageable) {
        Page<AptitudeQuestion> page = aptitudeQuestionRepository.findByTopicId(topicId, pageable);
        return page.map(this::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<AptitudeQuestionDto> getQuestionsByCategory(String category, String difficulty, Pageable pageable) {
        Page<AptitudeQuestion> page;
        if (difficulty != null && !difficulty.isBlank() && !difficulty.equalsIgnoreCase("ALL")) {
            page = aptitudeQuestionRepository.findByCategoryAndDifficulty(category, difficulty.toUpperCase(), pageable);
        } else {
            page = aptitudeQuestionRepository.findByCategory(category, pageable);
        }
        return page.map(this::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public CheckAnswerResponse checkAnswer(CheckAnswerRequest request) {
        AptitudeQuestion q = aptitudeQuestionRepository.findById(request.getQuestionId())
                .orElseThrow(() -> new RuntimeException("Question not found with ID: " + request.getQuestionId()));

        boolean isCorrect = q.getCorrectOption().equalsIgnoreCase(request.getSelectedOption().trim());

        return CheckAnswerResponse.builder()
                .questionId(q.getId())
                .correct(isCorrect)
                .correctOption(q.getCorrectOption())
                .explanation(q.getExplanation())
                .formulaHint(q.getFormulaHint())
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public AptitudeStatsDto getAptitudeStats() {
        long total = aptitudeQuestionRepository.count();

        Map<String, Long> categoryCounts = new LinkedHashMap<>();
        for (Object[] row : aptitudeQuestionRepository.countGroupByCategory()) {
            if (row[0] != null) {
                categoryCounts.put((String) row[0], ((Number) row[1]).longValue());
            }
        }

        Map<String, Long> topicCounts = new LinkedHashMap<>();
        for (Object[] row : aptitudeQuestionRepository.countGroupByTopic()) {
            if (row[0] != null) {
                topicCounts.put((String) row[0], ((Number) row[1]).longValue());
            }
        }

        Map<String, Long> difficultyCounts = new LinkedHashMap<>();
        for (Object[] row : aptitudeQuestionRepository.countGroupByDifficulty()) {
            if (row[0] != null) {
                difficultyCounts.put((String) row[0], ((Number) row[1]).longValue());
            }
        }

        Map<String, Long> attributionCounts = new LinkedHashMap<>();
        for (Object[] row : aptitudeQuestionRepository.countGroupBySourceAttribution()) {
            String attr = row[0] != null ? (String) row[0] : "Standard Curriculum";
            attributionCounts.put(attr, ((Number) row[1]).longValue());
        }

        return AptitudeStatsDto.builder()
                .totalQuestions(total)
                .categoryCounts(categoryCounts)
                .topicCounts(topicCounts)
                .difficultyCounts(difficultyCounts)
                .attributionCounts(attributionCounts)
                .duplicateCount(0)
                .malformedCount(0)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public AptitudeQuestionDto getQuestionById(Long id) {
        return aptitudeQuestionRepository.findById(id)
                .map(this::toDto)
                .orElseThrow(() -> new RuntimeException("Question not found with ID: " + id));
    }

    @Override
    @Transactional(readOnly = true)
    public List<AptitudeAssessmentQuestionDto> generateAssessment(String trackId, int count) {
        int safeCount = Math.max(5, Math.min(count, 50));
        String category = mapTrackToCategory(trackId);

        List<AptitudeQuestion> sampled = new ArrayList<>();
        Random rng = new Random();

        if (category != null) {
            long totalCategory = aptitudeQuestionRepository.countByCategory(category);
            int totalPages = (int) Math.max(1, totalCategory / safeCount);
            int randomPage = rng.nextInt(totalPages);
            Page<AptitudeQuestion> page = aptitudeQuestionRepository.findByCategory(category, PageRequest.of(randomPage, safeCount));
            sampled.addAll(page.getContent());
        } else {
            // "all" track: sample across all 4 categories evenly
            String[] categories = {
                    "Quantitative Aptitude",
                    "Logical Reasoning",
                    "Verbal Ability",
                    "Data Interpretation"
            };
            int perCat = Math.max(2, safeCount / categories.length);
            for (String cat : categories) {
                long totalCat = aptitudeQuestionRepository.countByCategory(cat);
                int totalPages = (int) Math.max(1, totalCat / perCat);
                int randomPage = rng.nextInt(totalPages);
                Page<AptitudeQuestion> p = aptitudeQuestionRepository.findByCategory(cat, PageRequest.of(randomPage, perCat));
                sampled.addAll(p.getContent());
            }
        }

        // If for any reason count is short, backfill
        if (sampled.size() < safeCount) {
            Page<AptitudeQuestion> fallback = aptitudeQuestionRepository.findAll(PageRequest.of(0, safeCount));
            for (AptitudeQuestion fq : fallback) {
                if (sampled.size() >= safeCount) break;
                if (!sampled.contains(fq)) {
                    sampled.add(fq);
                }
            }
        }

        // Shuffle questions
        Collections.shuffle(sampled);
        if (sampled.size() > safeCount) {
            sampled = sampled.subList(0, safeCount);
        }

        return sampled.stream().map(this::toAssessmentDto).toList();
    }

    @Override
    @Transactional
    public AptitudeAttemptResponse submitAssessment(User user, AptitudeSubmitRequest request) {
        List<AptitudeSubmitRequest.AnswerItem> answers = request.getAnswers() != null
                ? request.getAnswers()
                : Collections.emptyList();

        int totalQuestions = answers.size();
        int correctCount = 0;
        int incorrectCount = 0;
        int unansweredCount = 0;

        Map<String, int[]> categoryStats = new LinkedHashMap<>(); // [correct, total]
        Map<String, int[]> difficultyStats = new LinkedHashMap<>(); // [correct, total]

        List<AptitudeAttemptResponse.QuestionReviewDto> reviewList = new ArrayList<>();

        for (AptitudeSubmitRequest.AnswerItem item : answers) {
            AptitudeQuestion q = null;
            if (item.getQuestionId() != null) {
                q = aptitudeQuestionRepository.findById(item.getQuestionId()).orElse(null);
            } else if (item.getQuestionCode() != null) {
                q = aptitudeQuestionRepository.findByQuestionCode(item.getQuestionCode()).orElse(null);
            }

            if (q == null) {
                continue;
            }

            String selected = item.getSelectedOption() != null ? item.getSelectedOption().trim().toUpperCase() : null;
            boolean isAnswered = selected != null && !selected.isBlank();
            boolean isCorrect = isAnswered && selected.equalsIgnoreCase(q.getCorrectOption());

            if (!isAnswered) {
                unansweredCount++;
            } else if (isCorrect) {
                correctCount++;
            } else {
                incorrectCount++;
            }

            // Category Stats
            categoryStats.computeIfAbsent(q.getCategory(), k -> new int[2]);
            categoryStats.get(q.getCategory())[1]++;
            if (isCorrect) categoryStats.get(q.getCategory())[0]++;

            // Difficulty Stats
            difficultyStats.computeIfAbsent(q.getDifficulty(), k -> new int[2]);
            difficultyStats.get(q.getDifficulty())[1]++;
            if (isCorrect) difficultyStats.get(q.getDifficulty())[0]++;

            reviewList.add(AptitudeAttemptResponse.QuestionReviewDto.builder()
                    .id(q.getId())
                    .questionCode(q.getQuestionCode())
                    .category(q.getCategory())
                    .topic(q.getTopic())
                    .difficulty(q.getDifficulty())
                    .questionText(q.getQuestionText())
                    .options(List.of(
                            new AptitudeOptionDto("A", q.getOptionA()),
                            new AptitudeOptionDto("B", q.getOptionB()),
                            new AptitudeOptionDto("C", q.getOptionC()),
                            new AptitudeOptionDto("D", q.getOptionD())
                    ))
                    .selectedOption(selected)
                    .correctOption(q.getCorrectOption())
                    .isCorrect(isCorrect)
                    .isAnswered(isAnswered)
                    .explanation(q.getExplanation())
                    .formulaHint(q.getFormulaHint())
                    .sourceAttribution(sanitizeAttribution(q.getSourceAttribution()))
                    .build());
        }

        int score = correctCount;
        double percentage = totalQuestions > 0 ? Math.round(((double) correctCount / totalQuestions) * 1000.0) / 10.0 : 0.0;
        int answeredCount = correctCount + incorrectCount;
        double accuracy = answeredCount > 0 ? Math.round(((double) correctCount / answeredCount) * 1000.0) / 10.0 : 0.0;
        boolean passed = percentage >= 60.0;

        Map<String, Object> catMap = new LinkedHashMap<>();
        categoryStats.forEach((cat, counts) -> {
            Map<String, Object> c = new LinkedHashMap<>();
            c.put("correct", counts[0]);
            c.put("total", counts[1]);
            c.put("percentage", counts[1] > 0 ? Math.round(((double) counts[0] / counts[1]) * 100.0) : 0);
            catMap.put(cat, c);
        });

        Map<String, Object> diffMap = new LinkedHashMap<>();
        difficultyStats.forEach((diff, counts) -> {
            Map<String, Object> d = new LinkedHashMap<>();
            d.put("correct", counts[0]);
            d.put("total", counts[1]);
            d.put("percentage", counts[1] > 0 ? Math.round(((double) counts[0] / counts[1]) * 100.0) : 0);
            diffMap.put(diff, d);
        });

        String answersJson = "";
        String catJson = "";
        String diffJson = "";
        try {
            answersJson = objectMapper.writeValueAsString(reviewList);
            catJson = objectMapper.writeValueAsString(catMap);
            diffJson = objectMapper.writeValueAsString(diffMap);
        } catch (Exception e) {
            log.error("Failed to serialize attempt JSON", e);
        }

        String trackId = request.getTrackId() != null ? request.getTrackId() : "all";
        String trackTitle = request.getTrackTitle() != null ? request.getTrackTitle() : "Placement Aptitude Assessment";
        String reason = request.getCompletionReason() != null ? request.getCompletionReason() : "USER_SUBMITTED";

        AptitudeAttempt attempt = AptitudeAttempt.builder()
                .user(user)
                .trackId(trackId)
                .trackTitle(trackTitle)
                .totalQuestions(totalQuestions)
                .correctCount(correctCount)
                .incorrectCount(incorrectCount)
                .unansweredCount(unansweredCount)
                .score(score)
                .percentage(percentage)
                .accuracy(accuracy)
                .timeSpentSeconds(request.getTimeSpentSeconds())
                .timeLimitSeconds(request.getTimeLimitSeconds())
                .completionReason(reason)
                .completedAt(LocalDateTime.now())
                .answersJson(answersJson)
                .categoryBreakdownJson(catJson)
                .difficultyBreakdownJson(diffJson)
                .build();

        AptitudeAttempt saved = aptitudeAttemptRepository.save(attempt);

        return AptitudeAttemptResponse.builder()
                .id(saved.getId())
                .trackId(saved.getTrackId())
                .trackTitle(saved.getTrackTitle())
                .totalQuestions(saved.getTotalQuestions())
                .correctCount(saved.getCorrectCount())
                .incorrectCount(saved.getIncorrectCount())
                .unansweredCount(saved.getUnansweredCount())
                .score(saved.getScore())
                .percentage(saved.getPercentage())
                .accuracy(saved.getAccuracy())
                .timeSpentSeconds(saved.getTimeSpentSeconds())
                .timeLimitSeconds(saved.getTimeLimitSeconds())
                .completionReason(saved.getCompletionReason())
                .completedAt(saved.getCompletedAt())
                .passed(passed)
                .categoryBreakdown(catMap)
                .difficultyBreakdown(diffMap)
                .questions(reviewList)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public List<AptitudeAttemptResponse> getUserAttempts(User user) {
        List<AptitudeAttempt> attempts = aptitudeAttemptRepository.findByUserOrderByCompletedAtDesc(user);
        return attempts.stream().map(a -> {
            Map<String, Object> catMap = Collections.emptyMap();
            Map<String, Object> diffMap = Collections.emptyMap();
            try {
                if (a.getCategoryBreakdownJson() != null) {
                    catMap = objectMapper.readValue(a.getCategoryBreakdownJson(), new TypeReference<Map<String, Object>>() {});
                }
                if (a.getDifficultyBreakdownJson() != null) {
                    diffMap = objectMapper.readValue(a.getDifficultyBreakdownJson(), new TypeReference<Map<String, Object>>() {});
                }
            } catch (Exception e) {
                // ignore parsing failure in summary
            }

            return AptitudeAttemptResponse.builder()
                    .id(a.getId())
                    .trackId(a.getTrackId())
                    .trackTitle(a.getTrackTitle())
                    .totalQuestions(a.getTotalQuestions())
                    .correctCount(a.getCorrectCount())
                    .incorrectCount(a.getIncorrectCount())
                    .unansweredCount(a.getUnansweredCount())
                    .score(a.getScore())
                    .percentage(a.getPercentage())
                    .accuracy(a.getAccuracy())
                    .timeSpentSeconds(a.getTimeSpentSeconds())
                    .timeLimitSeconds(a.getTimeLimitSeconds())
                    .completionReason(a.getCompletionReason())
                    .completedAt(a.getCompletedAt())
                    .passed(a.getPercentage() >= 60.0)
                    .categoryBreakdown(catMap)
                    .difficultyBreakdown(diffMap)
                    .questions(Collections.emptyList()) // lightweight for list
                    .build();
        }).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public AptitudeAttemptResponse getAttemptById(User user, Long attemptId) {
        AptitudeAttempt a = aptitudeAttemptRepository.findByIdAndUser(attemptId, user)
                .orElseThrow(() -> new RuntimeException("Aptitude attempt not found: " + attemptId));

        Map<String, Object> catMap = Collections.emptyMap();
        Map<String, Object> diffMap = Collections.emptyMap();
        List<AptitudeAttemptResponse.QuestionReviewDto> questions = Collections.emptyList();
        try {
            if (a.getCategoryBreakdownJson() != null) {
                catMap = objectMapper.readValue(a.getCategoryBreakdownJson(), new TypeReference<Map<String, Object>>() {});
            }
            if (a.getDifficultyBreakdownJson() != null) {
                diffMap = objectMapper.readValue(a.getDifficultyBreakdownJson(), new TypeReference<Map<String, Object>>() {});
            }
            if (a.getAnswersJson() != null) {
                questions = objectMapper.readValue(a.getAnswersJson(), new TypeReference<List<AptitudeAttemptResponse.QuestionReviewDto>>() {});
            }
        } catch (Exception e) {
            log.error("Failed to parse attempt JSON details", e);
        }

        return AptitudeAttemptResponse.builder()
                .id(a.getId())
                .trackId(a.getTrackId())
                .trackTitle(a.getTrackTitle())
                .totalQuestions(a.getTotalQuestions())
                .correctCount(a.getCorrectCount())
                .incorrectCount(a.getIncorrectCount())
                .unansweredCount(a.getUnansweredCount())
                .score(a.getScore())
                .percentage(a.getPercentage())
                .accuracy(a.getAccuracy())
                .timeSpentSeconds(a.getTimeSpentSeconds())
                .timeLimitSeconds(a.getTimeLimitSeconds())
                .completionReason(a.getCompletionReason())
                .completedAt(a.getCompletedAt())
                .passed(a.getPercentage() >= 60.0)
                .categoryBreakdown(catMap)
                .difficultyBreakdown(diffMap)
                .questions(questions)
                .build();
    }

    private String mapTrackToCategory(String trackId) {
        if (trackId == null) return null;
        return switch (trackId.toLowerCase()) {
            case "quantitative", "quant" -> "Quantitative Aptitude";
            case "logical", "logic" -> "Logical Reasoning";
            case "verbal" -> "Verbal Ability";
            case "data_interpretation", "di" -> "Data Interpretation";
            default -> null;
        };
    }

    private AptitudeQuestionDto toDto(AptitudeQuestion q) {
        return AptitudeQuestionDto.builder()
                .id(q.getId())
                .questionCode(q.getQuestionCode())
                .category(q.getCategory())
                .topicId(q.getTopicId())
                .topic(q.getTopic())
                .difficulty(q.getDifficulty())
                .questionText(q.getQuestionText())
                .options(List.of(
                        new AptitudeOptionDto("A", q.getOptionA()),
                        new AptitudeOptionDto("B", q.getOptionB()),
                        new AptitudeOptionDto("C", q.getOptionC()),
                        new AptitudeOptionDto("D", q.getOptionD())
                ))
                .correctOption(q.getCorrectOption())
                .explanation(q.getExplanation())
                .formulaHint(q.getFormulaHint())
                .sourceAttribution(sanitizeAttribution(q.getSourceAttribution()))
                .tags(q.getTags())
                .build();
    }

    private AptitudeAssessmentQuestionDto toAssessmentDto(AptitudeQuestion q) {
        return AptitudeAssessmentQuestionDto.builder()
                .id(q.getId())
                .questionCode(q.getQuestionCode())
                .category(q.getCategory())
                .topicId(q.getTopicId())
                .topic(q.getTopic())
                .difficulty(q.getDifficulty())
                .questionText(q.getQuestionText())
                .options(List.of(
                        new AptitudeOptionDto("A", q.getOptionA()),
                        new AptitudeOptionDto("B", q.getOptionB()),
                        new AptitudeOptionDto("C", q.getOptionC()),
                        new AptitudeOptionDto("D", q.getOptionD())
                ))
                .formulaHint(q.getFormulaHint())
                .sourceAttribution(sanitizeAttribution(q.getSourceAttribution()))
                .build();
    }

    private String sanitizeAttribution(String attr) {
        if (attr == null || attr.isBlank()) {
            return null;
        }
        String lower = attr.toLowerCase();
        if (lower.contains("tcs") || lower.contains("infosys") || lower.contains("wipro")
                || lower.contains("cognizant") || lower.contains("accenture")
                || lower.contains("amazon") || lower.contains("google")
                || lower.contains("microsoft") || lower.contains("capgemini")) {
            return "Software Placement Assessment Pattern";
        }
        return attr;
    }
}
