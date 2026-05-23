package com.meta.foremeal.meallog.api.dto;

import java.util.List;

public record PredictionFoodSearchResponse(
        String sourceType,
        Long sourceId,
        Long foodId,
        Long recipeId,
        String name,
        Double calories,
        Double carbs,
        Double sugar,
        Double sodium,
        String nutritionSource,
        Double nutritionConfidence,
        List<String> nutritionWarnings
) {
}
