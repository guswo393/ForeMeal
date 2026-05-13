package com.meta.foremeal.user.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDate;

public class MyPageDto {

    public record UpdateRequest(
            @NotBlank
            @Size(max = 100)
            String username,

            @NotNull
            @Past
            LocalDate birthDate,

            @DecimalMin(value = "0.0", inclusive = false)
            BigDecimal heightCm,

            @DecimalMin(value = "0.0", inclusive = false)
            BigDecimal weightKg
    ) {
    }

    public record Response(
            Long userId,
            String email,
            String username,
            LocalDate birthDate,
            Integer birthYear,
            BigDecimal heightCm,
            BigDecimal weightKg
    ) {
    }
}
