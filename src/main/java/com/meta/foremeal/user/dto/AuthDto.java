package com.meta.foremeal.user.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class AuthDto {

    public record LoginRequest(
            @Email
            @NotBlank
            String email,

            @NotBlank
            @Size(min = 4, max = 255)
            String password
    ) {
    }

    public record LoginResponse(
            String accessToken,
            String tokenType,
            Long userId,
            String email,
            String username,
            String role
    ) {
    }
}