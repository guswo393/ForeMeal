package com.meta.foremeal.recommendation.dto;

import java.math.BigDecimal;

public class RecommendationDto {

    public record RecipeRecommendationResponse(
            Long recipeId,
            String title,
            String description,
            String category,
            String dishType,
            String difficulty,
            Integer cookingTime,
            Integer servings,
            BigDecimal totalCalories,
            String giLevel,
            int recommendScore,
            String recommendReason
    ) {}
}
