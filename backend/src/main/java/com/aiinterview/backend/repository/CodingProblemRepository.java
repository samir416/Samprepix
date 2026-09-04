package com.aiinterview.backend.repository;

import com.aiinterview.backend.entity.CodingProblem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface CodingProblemRepository
        extends JpaRepository<CodingProblem, Long> {

    List<CodingProblem> findByActiveTrue();

    List<CodingProblem> findByDifficultyAndActiveTrue(
            String difficulty
    );

    List<CodingProblem> findByMinimumExperienceLevelLessThanEqualAndActiveTrue(
            Integer experienceLevel
    );

    Optional<CodingProblem> findByTitleIgnoreCase(
            String title
    );

    Optional<CodingProblem> findBySlug(String slug);

    Optional<CodingProblem> findBySourceId(String sourceId);

    @Query("select p.sourceId from CodingProblem p where p.sourceId is not null")
    Set<String> findAllSourceIds();

    @Query("select p.slug from CodingProblem p where p.slug is not null")
    Set<String> findAllSlugs();

    @Query("select lower(p.title) from CodingProblem p")
    Set<String> findAllTitlesLower();

    @Query("select distinct tag from CodingProblem p join p.tags tag where p.active = true order by tag")
    List<String> findDistinctActiveTags();

    Page<CodingProblem> findByActiveTrue(Pageable pageable);

    Page<CodingProblem> findByDifficultyAndActiveTrue(
            String difficulty,
            Pageable pageable
    );

    @Query("select distinct p from CodingProblem p left join p.tags tag " +
            "where p.active = true and " +
            "(lower(p.title) like lower(concat('%', :search, '%')) or " +
            "lower(tag) like lower(concat('%', :search, '%'))) ")
    Page<CodingProblem> searchActiveProblems(
            @Param("search") String search,
            Pageable pageable
    );

    @Query("select distinct p from CodingProblem p left join p.tags tag " +
            "where p.active = true and p.difficulty = :difficulty and " +
            "(lower(p.title) like lower(concat('%', :search, '%')) or " +
            "lower(tag) like lower(concat('%', :search, '%'))) ")
    Page<CodingProblem> searchActiveProblemsByDifficulty(
            @Param("difficulty") String difficulty,
            @Param("search") String search,
            Pageable pageable
    );

    @Query("select distinct p from CodingProblem p left join p.tags tag " +
            "where p.active = true " +
            "and (:category is null or :category = '' or upper(p.category) = upper(:category)) " +
            "and (:difficulty is null or :difficulty = '' or p.difficulty = :difficulty) " +
            "and (:tag is null or :tag = '' or exists (select 1 from p.tags t where lower(t) = lower(:tag))) " +
            "and (:search is null or :search = '' or lower(p.title) like lower(concat('%', :search, '%')) or lower(tag) like lower(concat('%', :search, '%')))")
    Page<CodingProblem> searchActiveProblemsFiltered(
            @Param("category") String category,
            @Param("difficulty") String difficulty,
            @Param("tag") String tag,
            @Param("search") String search,
            Pageable pageable
    );
}