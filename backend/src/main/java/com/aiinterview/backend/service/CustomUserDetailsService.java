package com.aiinterview.backend.service;

import com.aiinterview.backend.entity.Role;
import com.aiinterview.backend.entity.User;
import com.aiinterview.backend.repository.UserRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class CustomUserDetailsService
        implements UserDetailsService {

    private final UserRepository userRepository;

    public CustomUserDetailsService(
            UserRepository userRepository
    ) {
        this.userRepository = userRepository;
    }

    @Override
    public UserDetails loadUserByUsername(
            String email
    ) throws UsernameNotFoundException {

        String normalizedEmail =
                email == null
                        ? ""
                        : email.trim().toLowerCase();

        if (normalizedEmail.isBlank()
                || normalizedEmail.length() > 254
                || normalizedEmail.contains("\r")
                || normalizedEmail.contains("\n")) {

            throw new UsernameNotFoundException(
                    "User not found"
            );
        }

        User user = userRepository
                .findByEmail(normalizedEmail)
                .orElseThrow(() ->
                        new UsernameNotFoundException(
                                "User not found"
                        )
                );

        String authority =
                user.getRole() == Role.ADMIN
                        ? "ROLE_ADMIN"
                        : "ROLE_USER";

        boolean enabled =
                user.isEmailVerified()
                        && user.getAccountStatus() != null
                        && "ACTIVE".equalsIgnoreCase(
                                user.getAccountStatus().name()
                        );

        return org.springframework.security.core.userdetails.User
                .builder()
                .username(user.getEmail())
                .password(
                        user.getPassword() != null
                                ? user.getPassword()
                                : ""
                )
                .authorities(authority)
                .disabled(!enabled)
                .accountExpired(false)
                .accountLocked(false)
                .credentialsExpired(false)
                .build();
    }
}