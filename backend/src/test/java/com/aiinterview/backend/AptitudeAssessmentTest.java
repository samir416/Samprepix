package com.aiinterview.backend;

import com.aiinterview.backend.dto.aptitude.AptitudeAssessmentQuestionDto;
import com.aiinterview.backend.dto.aptitude.AptitudeAttemptResponse;
import com.aiinterview.backend.dto.aptitude.AptitudeSubmitRequest;
import com.aiinterview.backend.entity.User;
import com.aiinterview.backend.repository.UserRepository;
import com.aiinterview.backend.service.aptitude.AptitudeQuestionService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
class AptitudeAssessmentTest {

    @Autowired
    private AptitudeQuestionService aptitudeQuestionService;

    @Autowired
    private UserRepository userRepository;

    @Test
    @DisplayName("Assessment generation, answer evaluation, persistence, and attempt history work end-to-end")
    void testAssessmentFlow() {
        User user = userRepository.findByEmail("samirprajapat5@gmail.com")
                .orElseGet(() -> userRepository.findAll().stream().findFirst().orElse(null));
        assertNotNull(user, "User must exist");

        // 1. Generate 10 quantitative assessment questions
        List<AptitudeAssessmentQuestionDto> questions = aptitudeQuestionService.generateAssessment("quantitative", 10);
        assertNotNull(questions);
        assertEquals(10, questions.size());

        for (AptitudeAssessmentQuestionDto q : questions) {
            assertNotNull(q.getId());
            assertNotNull(q.getQuestionCode());
            assertNotNull(q.getQuestionText());
            assertEquals(4, q.getOptions().size());
            assertEquals("Quantitative Aptitude", q.getCategory());
        }

        // 2. Generate mixed track assessment
        List<AptitudeAssessmentQuestionDto> mixed = aptitudeQuestionService.generateAssessment("all", 12);
        assertNotNull(mixed);
        assertEquals(12, mixed.size());

        // 3. Simulate answering
        List<AptitudeSubmitRequest.AnswerItem> answers = new ArrayList<>();
        // Answer first 8 questions with "A", leave 2 unanswered
        for (int i = 0; i < questions.size(); i++) {
            AptitudeAssessmentQuestionDto q = questions.get(i);
            String selected = i < 8 ? "A" : null;
            answers.add(AptitudeSubmitRequest.AnswerItem.builder()
                    .questionId(q.getId())
                    .questionCode(q.getQuestionCode())
                    .selectedOption(selected)
                    .build());
        }

        AptitudeSubmitRequest submitRequest = AptitudeSubmitRequest.builder()
                .trackId("quantitative")
                .trackTitle("Quantitative Aptitude Assessment")
                .timeSpentSeconds(350)
                .timeLimitSeconds(600)
                .completionReason("USER_SUBMITTED")
                .answers(answers)
                .build();

        // 4. Submit assessment
        AptitudeAttemptResponse result = aptitudeQuestionService.submitAssessment(user, submitRequest);
        assertNotNull(result);
        assertNotNull(result.getId());
        assertEquals(10, result.getTotalQuestions());
        assertEquals(2, result.getUnansweredCount());
        assertEquals(8, result.getCorrectCount() + result.getIncorrectCount());
        assertEquals(result.getScore(), result.getCorrectCount());
        assertTrue(result.getPercentage() >= 0.0 && result.getPercentage() <= 100.0);
        assertEquals(350, result.getTimeSpentSeconds());
        assertEquals("USER_SUBMITTED", result.getCompletionReason());
        assertNotNull(result.getQuestions());
        assertEquals(10, result.getQuestions().size());

        // 5. Query user attempts
        List<AptitudeAttemptResponse> attempts = aptitudeQuestionService.getUserAttempts(user);
        assertNotNull(attempts);
        assertFalse(attempts.isEmpty());
        assertTrue(attempts.stream().anyMatch(a -> a.getId().equals(result.getId())));

        // 6. Query attempt by id
        AptitudeAttemptResponse fetched = aptitudeQuestionService.getAttemptById(user, result.getId());
        assertNotNull(fetched);
        assertEquals(result.getId(), fetched.getId());
        assertEquals(10, fetched.getQuestions().size());
    }
}
