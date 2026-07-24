package com.aiinterview.backend.repository;

import com.aiinterview.backend.entity.User;
import com.aiinterview.backend.entity.UserProfile;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserProfileRepository extends JpaRepository<UserProfile, Long> {

    Optional<UserProfile> findByUser(User user);

}