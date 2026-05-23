package com.meta.foremeal.meallog.api.dto;

public record PredictionFoodSearchResponse(
        String sourceType,
        Long sourceId,
        Long foodId,
        Long recipeId,
        String name,
        Double calories,
        Double carbs,
        Double sugar,
        Double sodium
) {
}
