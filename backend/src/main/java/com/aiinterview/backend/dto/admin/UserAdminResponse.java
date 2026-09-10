package com.aiinterview.backend.dto.admin;

import com.aiinterview.backend.entity.AccountStatus;
import com.aiinterview.backend.entity.Role;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserAdminResponse {
    private Long id;
    private String username;
    private String email;
    private String name;
    private Role role;
    private AccountStatus accountStatus;
    private boolean emailVerified;
    private String provider;
    private LocalDateTime createdAt;
    private String currentPlan;
    private String subscriptionStatus;
}
