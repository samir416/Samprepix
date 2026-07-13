package com.aiinterview.backend.repository;

import com.aiinterview.backend.entity.InterviewSession;
import com.aiinterview.backend.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface InterviewSessionRepository extends JpaRepository<InterviewSession, Long> {

    List<InterviewSession> findByUser(User user);

}