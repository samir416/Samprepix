package com.aiinterview.backend.repository;

import com.aiinterview.backend.entity.AptitudeQuestion;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface AptitudeQuestionRepository extends JpaRepository<AptitudeQuestion, Long> {

    Optional<AptitudeQuestion> findByQuestionCode(String questionCode);

    Page<AptitudeQuestion> findByTopicId(String topicId, Pageable pageable);

    Page<AptitudeQuestion> findByCategory(String category, Pageable pageable);

    Page<AptitudeQuestion> findByCategoryAndDifficulty(String category, String difficulty, Pageable pageable);

    Page<AptitudeQuestion> findByTopicIdAndDifficulty(String topicId, String difficulty, Pageable pageable);

    long countByCategory(String category);

    long countByTopicId(String topicId);

    long countByDifficulty(String difficulty);

    @Query("SELECT q.category, COUNT(q) FROM AptitudeQuestion q GROUP BY q.category")
    List<Object[]> countGroupByCategory();

    @Query("SELECT q.topic, COUNT(q) FROM AptitudeQuestion q GROUP BY q.topic")
    List<Object[]> countGroupByTopic();

    @Query("SELECT q.difficulty, COUNT(q) FROM AptitudeQuestion q GROUP BY q.difficulty")
    List<Object[]> countGroupByDifficulty();

    @Query("SELECT COALESCE(q.sourceAttribution, 'Unattributed'), COUNT(q) FROM AptitudeQuestion q GROUP BY q.sourceAttribution")
    List<Object[]> countGroupBySourceAttribution();

    @Query("SELECT q.topicId, COUNT(q) FROM AptitudeQuestion q GROUP BY q.topicId")
    List<Object[]> countGroupByTopicId();
}
