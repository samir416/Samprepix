package com.aiinterview.backend;

import com.aiinterview.backend.dto.support.ReportProblemRequest;
import com.aiinterview.backend.dto.support.SupportQuestionResponse;
import com.aiinterview.backend.entity.User;
import com.aiinterview.backend.repository.UserRepository;
import com.aiinterview.backend.service.EmailService;
import com.aiinterview.backend.service.impl.SupportServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

public class SupportServiceTest {

    private EmailService emailService;
    private UserRepository userRepository;
    private SupportServiceImpl supportService;

    @BeforeEach
    void setUp() {
        emailService = mock(EmailService.class);
        userRepository = mock(UserRepository.class);
        supportService = new SupportServiceImpl(emailService, userRepository);
    }

    @Test
    void testSubmitProblemReport_DispatchesEmail() {
        ReportProblemRequest request = new ReportProblemRequest(
                "Coding Arena",
                "Compilation timed out on test case 4",
                "Submitting Python code",
                "dev@example.com",
                "/coding-arena/two-sum"
        );

        User mockUser = new User();
        mockUser.setUsername("testuser");
        mockUser.setEmail("testuser@example.com");
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(mockUser));

        supportService.submitProblemReport(request, "testuser");

        verify(emailService, times(1)).sendProblemReportEmail(
                anyString(),
                eq("dev@example.com"),
                eq("testuser"),
                eq("Coding Arena"),
                contains("Compilation timed out"),
                eq("Submitting Python code"),
                eq("/coding-arena/two-sum")
        );
    }

    @Test
    void testAnswerQuestion_GroundedKnowledgeBase() {
        SupportQuestionResponse roadmapResp = supportService.answerQuestion("How does the AI Roadmap work?");
        assertNotNull(roadmapResp);
        assertTrue(roadmapResp.getAnswer().contains("AI Roadmap"));
        assertEquals("knowledge_base", roadmapResp.getSource());

        SupportQuestionResponse githubResp = supportService.answerQuestion("Tell me about GitHub Analyzer");
        assertNotNull(githubResp);
        assertTrue(githubResp.getAnswer().contains("GitHub Profile Analyzer"));

        SupportQuestionResponse pricingResp = supportService.answerQuestion("What is the difference between PRO and ELITE?");
        assertNotNull(pricingResp);
        assertTrue(pricingResp.getAnswer().contains("PRO"));
        assertTrue(pricingResp.getAnswer().contains("ELITE"));
    }
}
