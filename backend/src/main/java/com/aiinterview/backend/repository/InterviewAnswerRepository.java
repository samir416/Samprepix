package com.aiinterview.backend.repository;

import com.aiinterview.backend.entity.InterviewAnswer;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface InterviewAnswerRepository
        extends JpaRepository<InterviewAnswer, Long> {

    List<InterviewAnswer> findBySessionIdOrderByQuestionNumberAsc(Long sessionId);

}