package com.aiinterview.backend.repository;

import com.aiinterview.backend.entity.Plan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PlanRepository extends JpaRepository<Plan, Long> {

    boolean existsByName(String name);

    boolean existsByNameIgnoreCase(String name);

    List<Plan> findByActiveTrue();

    List<Plan> findByActiveTrueOrderByIdAsc();

    Optional<Plan> findByNameAndActiveTrue(String name);

    Optional<Plan> findByNameIgnoreCaseAndActiveTrue(String name);

    Optional<Plan> findByName(String name);

    Optional<Plan> findByNameIgnoreCase(String name);
}