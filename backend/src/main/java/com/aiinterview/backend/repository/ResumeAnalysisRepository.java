package com.aiinterview.backend.repository;

import com.aiinterview.backend.entity.ResumeAnalysis;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ResumeAnalysisRepository
        extends JpaRepository<ResumeAnalysis, Long> {

        List<ResumeAnalysis> findByUserEmail(String userEmail);

        ResumeAnalysis findTopByUserEmailOrderByIdDesc(String userEmail);
}