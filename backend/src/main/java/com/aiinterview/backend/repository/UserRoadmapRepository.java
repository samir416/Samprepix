package com.aiinterview.backend.repository;

import com.aiinterview.backend.entity.User;
import com.aiinterview.backend.entity.UserRoadmap;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRoadmapRepository extends JpaRepository<UserRoadmap, Long> {

    List<UserRoadmap> findByUserOrderByUpdatedAtDesc(User user);

    Optional<UserRoadmap> findFirstByUserOrderByUpdatedAtDesc(User user);

    Optional<UserRoadmap> findByUserAndTrackId(User user, String trackId);
}
