package com.meta.foremeal.meallog.api.dto;

import com.meta.foremeal.meallog.domain.MealSource;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

public class MealLogDto {

    public record CreateRequest(
            @NotNull Long userId,
            @NotNull LocalDateTime eatenAt,
            String notes,
            @NotNull MealSource source,
            Long recipeId
    ) {}

    public record Response(
            Long mealId,
            Long userId,
            LocalDateTime eatenAt,
            String notes,
            MealSource source,
            Long recipeId
    ) {}
}
