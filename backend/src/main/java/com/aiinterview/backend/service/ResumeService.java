package com.aiinterview.backend.service;

import com.aiinterview.backend.model.ResumeResponse;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ResumeService {

    public ResumeResponse analyzeResume() {

        ResumeResponse response =
                new ResumeResponse();

        response.setScore(78);

        response.setSkills(
                List.of(
                        "Java",
                        "SQL",
                        "HTML",
                        "CSS"
                )
        );

        response.setMissingSkills(
                List.of(
                        "Spring Boot",
                        "React"
                )
        );

        response.setSuggestions(
                List.of(
                        "Add more projects",
                        "Improve resume summary",
                        "Add GitHub profile"
                )
        );

        return response;
    }
}