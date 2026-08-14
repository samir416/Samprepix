package com.aiinterview.backend.repository;

import com.aiinterview.backend.entity.CodingProgress;
import com.aiinterview.backend.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CodingProgressRepository
        extends JpaRepository<CodingProgress, Long> {

    Optional<CodingProgress> findByUser(User user);

    Optional<CodingProgress> findByUserId(Long userId);

    boolean existsByUserId(Long userId);
}