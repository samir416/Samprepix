package com.aiinterview.backend.model;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ResetPasswordRequest {

    @NotBlank
    private String token;

    @NotBlank
    private String password;
}