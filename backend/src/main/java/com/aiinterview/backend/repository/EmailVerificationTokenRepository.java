package com.aiinterview.backend.repository;

import com.aiinterview.backend.entity.EmailVerificationToken;
import com.aiinterview.backend.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface EmailVerificationTokenRepository extends JpaRepository<EmailVerificationToken, Long> {

    Optional<EmailVerificationToken> findByUser(User user);

    Optional<EmailVerificationToken> findByOtp(String otp);

    void deleteByUser(User user);
}