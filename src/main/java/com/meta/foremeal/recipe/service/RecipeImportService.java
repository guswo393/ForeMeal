package com.meta.foremeal.recipe.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.meta.foremeal.recipe.domain.Recipe;
import com.meta.foremeal.recipe.domain.RecipeIngredient;
import com.meta.foremeal.recipe.domain.RecipeStep;
import com.meta.foremeal.recipe.external.FoodSafetyRecipeClient;
import com.meta.foremeal.recipe.external.FoodSafetyRecipeDto;
import com.meta.foremeal.recipe.parser.ParsedIngredient;
import com.meta.foremeal.recipe.parser.RecipeIngredientParser;
import com.meta.foremeal.recipe.repo.RecipeRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class RecipeImportService {

    private static final String SOURCE_FOOD_SAFETY = "FOOD_SAFETY_KOREA";

    private final FoodSafetyRecipeClient foodSafetyRecipeClient;
    private final RecipeRepository recipeRepository;
    private final ObjectMapper objectMapper;
    private final RecipeIngredientParser recipeIngredientParser;
    private final RecipeIngredientFoodMatcher recipeIngredientFoodMatcher;

    public RecipeImportService(FoodSafetyRecipeClient foodSafetyRecipeClient,
                               RecipeRepository recipeRepository,
                               ObjectMapper objectMapper,
                               RecipeIngredientParser recipeIngredientParser,
                               RecipeIngredientFoodMatcher recipeIngredientFoodMatcher) {
        this.foodSafetyRecipeClient = foodSafetyRecipeClient;
        this.recipeRepository = recipeRepository;
        this.objectMapper = objectMapper;
        this.recipeIngredientParser = recipeIngredientParser;
        this.recipeIngredientFoodMatcher = recipeIngredientFoodMatcher;
    }

    @Transactional
    public ImportResult importFoodSafetyRecipes(int start, int end) {
        FoodSafetyRecipeDto response = foodSafetyRecipeClient.fetch(start, end);
        List<FoodSafetyRecipeDto.Row> rows = response == null ? List.of() : response.rows();

        int imported = 0;
        int skipped = 0;

        for (FoodSafetyRecipeDto.Row row : rows) {
            String externalId = row.recipeSeq();
            if (externalId == null || recipeRepository.existsBySourceAndExternalId(SOURCE_FOOD_SAFETY, externalId)) {
                skipped++;
                continue;
            }

            Recipe recipe = toRecipe(row);
            recipeRepository.save(recipe);
            imported++;
        }

        return new ImportResult(rows.size(), imported, skipped);
    }

    private Recipe toRecipe(FoodSafetyRecipeDto.Row row) {
        Recipe recipe = new Recipe(
                defaultText(row.recipeName(), "외부 레시피"),
                row.recipeSeq(),
                SOURCE_FOOD_SAFETY,
                buildDescription(row),
                row.category(),
                row.cookingWay(),
                null,
                null,
                1,
                parseDecimal(row.calories()),
                toNutrientsJson(row),
                null,
                row.mainImageUri()
        );

        String ingredientInfo = row.ingredientInfo();
        if (ingredientInfo != null) {
            List<ParsedIngredient> parsedIngredients = recipeIngredientParser.parse(ingredientInfo);

            if (parsedIngredients.isEmpty()) {
                String ingredientName = truncate(ingredientInfo, 255);
                recipe.addIngredient(new RecipeIngredient(
                        recipeIngredientFoodMatcher.matchFoodId(ingredientName),
                        ingredientName,
                        null,
                        null
                ));
            } else {
                parsedIngredients.forEach(ingredient -> recipe.addIngredient(new RecipeIngredient(
                        recipeIngredientFoodMatcher.matchFoodId(ingredient.ingredientName()),
                        truncate(ingredient.ingredientName(), 255),
                        ingredient.quantity(),
                        ingredient.unit()
                )));
            }
        }

        for (int i = 1; i <= 20; i++) {
            String instruction = row.manual(i);
            if (instruction != null) {
                recipe.addStep(new RecipeStep(i, instruction, row.manualImage(i)));
            }
        }

        return recipe;
    }

    private String buildDescription(FoodSafetyRecipeDto.Row row) {
        String hashTag = row.hashTag();
        return hashTag == null ? "식품의약품안전처 공개 레시피를 바탕으로 정리한 레시피입니다." : hashTag;
    }

    private String toNutrientsJson(FoodSafetyRecipeDto.Row row) {
        Map<String, BigDecimal> nutrients = new LinkedHashMap<>();
        nutrients.put("carbs", parseDecimal(row.carbs()));
        nutrients.put("protein", parseDecimal(row.protein()));
        nutrients.put("fat", parseDecimal(row.fat()));
        nutrients.put("sodium", parseDecimal(row.sodium()));

        try {
            return objectMapper.writeValueAsString(nutrients);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Failed to serialize recipe nutrients.", e);
        }
    }

    private BigDecimal parseDecimal(String value) {
        if (value == null) {
            return null;
        }

        try {
            return new BigDecimal(value.replaceAll("[^0-9.]", ""));
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private String defaultText(String value, String fallback) {
        return value == null ? fallback : value;
    }

    private String truncate(String value, int maxLength) {
        if (value == null || value.length() <= maxLength) {
            return value;
        }
        return value.substring(0, maxLength);
    }

    public record ImportResult(
            int fetched,
            int imported,
            int skipped
    ) {
    }
}
