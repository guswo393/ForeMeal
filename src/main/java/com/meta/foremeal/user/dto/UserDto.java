package com.meta.foremeal.user.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public class UserDto {

    public record CreateRequest(
            @Email
            @NotBlank
            String email,

            @NotBlank
            @Size(min = 4, max = 255)
            String password,

            @NotBlank
            @Size(max = 100)
            String username,

            @NotNull
            Integer birthYear,

            @Past
            LocalDate birthDate
    ) {
    }

    public record UpdateRequest(
            @NotBlank
            @Size(max = 100)
            String username,

            @NotNull
            Integer birthYear,

            @Past
            LocalDate birthDate
    ) {
    }

    public record ChangePasswordRequest(
            @NotBlank
            @Size(min = 4, max = 255)
            String currentPassword,

            @NotBlank
            @Size(min = 4, max = 255)
            String newPassword
    ) {
    }

    public record DeleteRequest(
            @NotBlank
            @Size(min = 4, max = 255)
            String password
    ) {
    }

    public record Response(
            Long userId,
            String email,
            String username,
            Integer birthYear,
            LocalDate birthDate
    ) {
    }
}
