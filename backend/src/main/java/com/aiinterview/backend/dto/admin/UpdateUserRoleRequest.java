package com.aiinterview.backend.dto.admin;

import com.aiinterview.backend.entity.Role;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateUserRoleRequest {
    private Role role;
}
