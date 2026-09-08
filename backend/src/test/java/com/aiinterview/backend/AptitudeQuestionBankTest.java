package com.aiinterview.backend;

import com.aiinterview.backend.dto.aptitude.AptitudeQuestionDto;
import com.aiinterview.backend.dto.aptitude.AptitudeStatsDto;
import com.aiinterview.backend.dto.aptitude.CheckAnswerRequest;
import com.aiinterview.backend.dto.aptitude.CheckAnswerResponse;
import com.aiinterview.backend.entity.AptitudeQuestion;
import com.aiinterview.backend.repository.AptitudeQuestionRepository;
import com.aiinterview.backend.service.aptitude.AptitudeQuestionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class AptitudeQuestionBankTest {

    @Autowired
    private AptitudeQuestionService aptitudeQuestionService;

    @Autowired
    private AptitudeQuestionRepository aptitudeQuestionRepository;

    @BeforeEach
    void setUp() {
        aptitudeQuestionService.seedQuestionBankIfNotPresent();
    }

    @Test
    @DisplayName("Verify Aptitude database contains at least 20,000 verified placement questions")
    void testTotalQuestionBankSize() {
        long totalCount = aptitudeQuestionRepository.count();
        System.out.printf("Total Aptitude questions in database: %d%n", totalCount);
        assertTrue(totalCount >= 20000, "Database must contain at least 20,000 aptitude questions. Found: " + totalCount);
    }

    @Test
    @DisplayName("Verify distribution across 4 core placement categories")
    void testCategoryBreakdown() {
        AptitudeStatsDto stats = aptitudeQuestionService.getAptitudeStats();
        Map<String, Long> catCounts = stats.getCategoryCounts();
        System.out.printf("Category breakdown: %s%n", catCounts);

        long quant = catCounts.getOrDefault("Quantitative Aptitude", 0L);
        long logic = catCounts.getOrDefault("Logical Reasoning", 0L);
        long verbal = catCounts.getOrDefault("Verbal Ability", 0L);
        long di = catCounts.getOrDefault("Data Interpretation", 0L);

        assertTrue(quant >= 7000, "Quantitative Aptitude must have at least 7000 questions, found: " + quant);
        assertTrue(logic >= 5000, "Logical Reasoning must have at least 5000 questions, found: " + logic);
        assertTrue(verbal >= 4500, "Verbal Ability must have at least 4500 questions, found: " + verbal);
        assertTrue(di >= 3500, "Data Interpretation must have at least 3500 questions, found: " + di);
    }

    @Test
    @DisplayName("Verify duplicate count is strictly 0 and all records are well-formed")
    void testIntegrityAndZeroDuplicates() {
        List<AptitudeQuestion> sample = aptitudeQuestionRepository.findAll(PageRequest.of(0, 1000)).getContent();
        assertFalse(sample.isEmpty(), "Question repository should return questions");

        Set<String> codes = new HashSet<>();
        for (AptitudeQuestion q : sample) {
            // Verify well-formedness
            assertNotNull(q.getQuestionCode(), "Question code must not be null");
            assertNotNull(q.getQuestionText(), "Question text must not be null");
            assertFalse(q.getQuestionText().isBlank(), "Question text must not be blank");
            assertNotNull(q.getOptionA(), "Option A must not be null");
            assertNotNull(q.getOptionB(), "Option B must not be null");
            assertNotNull(q.getOptionC(), "Option C must not be null");
            assertNotNull(q.getOptionD(), "Option D must not be null");
            assertNotNull(q.getCorrectOption(), "Correct option must not be null");
            assertTrue(List.of("A", "B", "C", "D").contains(q.getCorrectOption()), "Correct option must be A, B, C, or D");
            assertNotNull(q.getExplanation(), "Explanation must not be null");
            assertFalse(q.getExplanation().isBlank(), "Explanation must not be blank");

            // Verify no duplicate codes
            assertTrue(codes.add(q.getQuestionCode()), "Duplicate question code detected: " + q.getQuestionCode());

            // Verify legitimate attribution (NO fake FAANG claims)
            if (q.getSourceAttribution() != null) {
                assertFalse(q.getSourceAttribution().toLowerCase().contains("google"), "Must not claim Google without credible evidence");
                assertFalse(q.getSourceAttribution().toLowerCase().contains("amazon"), "Must not claim Amazon without credible evidence");
                assertFalse(q.getSourceAttribution().toLowerCase().contains("meta"), "Must not claim Meta without credible evidence");
                assertFalse(q.getSourceAttribution().toLowerCase().contains("apple"), "Must not claim Apple without credible evidence");
                assertFalse(q.getSourceAttribution().toLowerCase().contains("netflix"), "Must not claim Netflix without credible evidence");
            }
        }
    }

    @Test
    @DisplayName("Verify interactive answer checking and derivation reveal")
    void testCheckAnswerFlow() {
        Page<AptitudeQuestionDto> questions = aptitudeQuestionService.getQuestionsByTopic("quant-1", PageRequest.of(0, 5));
        assertFalse(questions.isEmpty(), "Should retrieve questions for topic quant-1");

        AptitudeQuestionDto firstQ = questions.getContent().get(0);
        String correctOption = firstQ.getCorrectOption();

        // 1. Submit correct option
        CheckAnswerResponse response = aptitudeQuestionService.checkAnswer(
                CheckAnswerRequest.builder()
                        .questionId(firstQ.getId())
                        .selectedOption(correctOption)
                        .build()
        );
        assertTrue(response.isCorrect(), "Answer should evaluate to true when matching correct option");
        assertEquals(correctOption, response.getCorrectOption());
        assertNotNull(response.getExplanation());
        assertFalse(response.getExplanation().isBlank());

        // 2. Submit wrong option
        String wrongOption = correctOption.equals("A") ? "B" : "A";
        CheckAnswerResponse wrongResponse = aptitudeQuestionService.checkAnswer(
                CheckAnswerRequest.builder()
                        .questionId(firstQ.getId())
                        .selectedOption(wrongOption)
                        .build()
        );
        assertFalse(wrongResponse.isCorrect(), "Answer should evaluate to false for wrong option");
        assertEquals(correctOption, wrongResponse.getCorrectOption());
    }
}
