package com.aiinterview.backend.service.history;

import com.aiinterview.backend.entity.ResumeAnalysis;
import com.aiinterview.backend.repository.ResumeAnalysisRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ResumeHistoryService {

    private final ResumeAnalysisRepository repository;

    public ResumeHistoryService(ResumeAnalysisRepository repository) {
        this.repository = repository;
    }

    /**
     * Save analysis
     */
    public ResumeAnalysis save(ResumeAnalysis analysis) {

        if (analysis == null) {
            throw new IllegalArgumentException("Resume analysis cannot be null.");
        }

        return repository.save(analysis);
    }

    /**
     * History of specific user
     */
    public List<ResumeAnalysis> getHistory(String userEmail) {

        return repository.findByUserEmail(userEmail);
    }

    /**
     * Latest analysis of specific user
     */
    public ResumeAnalysis getLatestAnalysis(String userEmail) {

        return repository.findTopByUserEmailOrderByIdDesc(userEmail);
    }

    /**
     * Delete by id
     */
    public void delete(Long id) {

        repository.deleteById(id);
    }

    /**
     * Find by id
     */
    public ResumeAnalysis findById(Long id) {

        return repository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException("Resume analysis not found."));
    }

}