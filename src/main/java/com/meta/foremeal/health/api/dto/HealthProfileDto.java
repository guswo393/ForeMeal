package com.meta.foremeal.health.api.dto;

import com.meta.foremeal.health.domain.HealthGoal;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public class HealthProfileDto {

    public record UpsertRequest(
            @NotNull Boolean hasDiabetes,
            @NotNull Boolean hasHypertension,
            @DecimalMin(value = "0.0", inclusive = false) BigDecimal heightCm,
            @DecimalMin(value = "0.0", inclusive = false) BigDecimal weightKg,
            HealthGoal goal,
            @Size(max = 2000) String allergyNotes,
            @Size(max = 2000) String avoidIngredients
    ) {
    }

    public record Response(
            Long healthProfileId,
            Long userId,
            boolean hasDiabetes,
            boolean hasHypertension,
            BigDecimal heightCm,
            BigDecimal weightKg,
            HealthGoal goal,
            String allergyNotes,
            String avoidIngredients
    ) {
    }
}
