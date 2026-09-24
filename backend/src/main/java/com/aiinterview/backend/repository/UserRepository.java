package com.aiinterview.backend.repository;

import com.aiinterview.backend.entity.AccountStatus;
import com.aiinterview.backend.entity.Role;
import com.aiinterview.backend.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);

    Optional<User> findByUsername(String username);

    boolean existsByEmail(String email);

    boolean existsByUsername(String username);

    Page<User> findByRole(
            Role role,
            Pageable pageable
    );

    Page<User> findByAccountStatus(
            AccountStatus accountStatus,
            Pageable pageable
    );

    Page<User> findByRoleAndAccountStatus(
            Role role,
            AccountStatus accountStatus,
            Pageable pageable
    );

    long countByAccountStatus(
            AccountStatus accountStatus
    );

    Page<User> findAll(Pageable pageable);
}