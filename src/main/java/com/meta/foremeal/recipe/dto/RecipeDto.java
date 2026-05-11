package com.meta.foremeal.recipe.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.util.List;

public class RecipeDto {

    public record IngredientRequest(
            Long foodId,
            @NotBlank @Size(max = 255) String ingredientName,
            BigDecimal quantity,
            @Size(max = 30) String unit
    ) {}

    public record StepRequest(
            @NotNull Integer stepOrder,
            @NotBlank String instruction,
            @Size(max = 500) String imageUri
    ) {}

    public record CreateRequest(
            @NotBlank @Size(max = 255) String title,
            String description,
            @Size(max = 100) String category,
            @Size(max = 100) String dishType,
            @Size(max = 50) String difficulty,
            Integer cookingTime,
            Integer servings,
            BigDecimal totalCalories,
            String totalNutrients,
            @Size(max = 50) String giLevel,
            @Valid List<IngredientRequest> ingredients,
            @Valid List<StepRequest> steps
    ) {}

    public record UpdateRequest(
            @NotBlank @Size(max = 255) String title,
            String description,
            @Size(max = 100) String category,
            @Size(max = 100) String dishType,
            @Size(max = 50) String difficulty,
            Integer cookingTime,
            Integer servings,
            BigDecimal totalCalories,
            String totalNutrients,
            @Size(max = 50) String giLevel,
            @Valid List<IngredientRequest> ingredients,
            @Valid List<StepRequest> steps
    ) {}

    public record IngredientResponse(
            Long itemId,
            Long foodId,
            String ingredientName,
            BigDecimal quantity,
            String unit
    ) {}

    public record StepResponse(
            Long stepId,
            Integer stepOrder,
            String instruction,
            String imageUri
    ) {}

    public record Response(
            Long recipeId,
            String title,
            String description,
            String category,
            String dishType,
            String difficulty,
            Integer cookingTime,
            Integer servings,
            BigDecimal totalCalories,
            String totalNutrients,
            String giLevel,
            List<IngredientResponse> ingredients,
            List<StepResponse> steps
    ) {}
}
