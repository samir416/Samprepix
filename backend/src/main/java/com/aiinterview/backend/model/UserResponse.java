package com.aiinterview.backend.model;

import com.aiinterview.backend.entity.UserRole;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor

public class UserResponse {

    private Long id;
    private String username;
    private String name;
    private String email;
    private String profilePicture;
    private boolean profileCompleted;
    private String role;
    private String plan;

}