package com.aiinterview.backend.service.aptitude;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component
@Order(10)
@RequiredArgsConstructor
public class AptitudeInitializer implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(AptitudeInitializer.class);
    private final AptitudeQuestionService aptitudeQuestionService;

    @Override
    public void run(String... args) {
        try {
            log.info("Checking Aptitude Question Bank initialization status...");
            aptitudeQuestionService.seedQuestionBankIfNotPresent();
        } catch (Exception e) {
            log.error("Failed to initialize Aptitude Question Bank: {}", e.getMessage(), e);
        }
    }
}
