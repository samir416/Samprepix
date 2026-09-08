package com.aiinterview.backend.repository;

import com.aiinterview.backend.entity.AptitudeAttempt;
import com.aiinterview.backend.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface AptitudeAttemptRepository extends JpaRepository<AptitudeAttempt, Long> {

    List<AptitudeAttempt> findByUserOrderByCompletedAtDesc(User user);

    Optional<AptitudeAttempt> findByIdAndUser(Long id, User user);

    long countByUser(User user);

    @Query("SELECT MAX(a.percentage) FROM AptitudeAttempt a WHERE a.user = :user")
    Double findBestPercentageByUser(@Param("user") User user);

    @Query("SELECT AVG(a.percentage) FROM AptitudeAttempt a WHERE a.user = :user")
    Double findAvgPercentageByUser(@Param("user") User user);
}
