package com.aiinterview.backend.repository;

import com.aiinterview.backend.entity.GithubAnalysisResult;
import com.aiinterview.backend.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface GithubAnalysisResultRepository extends JpaRepository<GithubAnalysisResult, Long> {

    List<GithubAnalysisResult> findByUserOrderByAnalyzedAtDesc(User user);

    Optional<GithubAnalysisResult> findFirstByUserOrderByAnalyzedAtDesc(User user);
}
