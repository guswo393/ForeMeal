package com.meta.foremeal.meallog.api.dto;

import com.meta.foremeal.meallog.domain.MealSource;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public class MealLogDto {

    public record ItemRequest(
            Long foodId,
            @NotNull @Size(max = 255) String foodName,
            @NotNull BigDecimal quantity,
            @NotNull @Size(max = 30) String unit
    ) {}

    public record CreateRequest(
            @NotNull Long userId,
            @NotNull LocalDateTime eatenAt,
            String notes,
            @NotNull MealSource source,
            Long recipeId,
            @NotNull @Valid List<ItemRequest> items
    ) {}

    public record ItemResponse(
            Long itemId,
            Long foodId,
            String foodName,
            BigDecimal quantity,
            String unit
    ) {}

    public record Response(
            Long mealId,
            Long userId,
            LocalDateTime eatenAt,
            String notes,
            MealSource source,
            Long recipeId,
            List<ItemResponse> items
    ) {}

    public record DailySummaryResponse(
            Long userId,
            String date,
            BigDecimal totalCalories,
            BigDecimal totalSodium,
            BigDecimal totalSugar,
            BigDecimal totalCarbs
    ) {}
}
