package com.meta.foremeal.recipe.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.meta.foremeal.recipe.domain.Recipe;
import com.meta.foremeal.recipe.external.FoodSafetyRecipeClient;
import com.meta.foremeal.recipe.external.FoodSafetyRecipeDto;
import com.meta.foremeal.recipe.parser.RecipeIngredientParser;
import com.meta.foremeal.recipe.repo.RecipeRepository;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.lang.reflect.Field;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class RecipeImportServiceTest {

    private final FoodSafetyRecipeClient foodSafetyRecipeClient = mock(FoodSafetyRecipeClient.class);
    private final RecipeRepository recipeRepository = mock(RecipeRepository.class);
    private final RecipeIngredientFoodMatcher recipeIngredientFoodMatcher = mock(RecipeIngredientFoodMatcher.class);
    private final RecipeImportService recipeImportService = new RecipeImportService(
            foodSafetyRecipeClient,
            recipeRepository,
            new ObjectMapper(),
            new RecipeIngredientParser(),
            recipeIngredientFoodMatcher
    );

    @Test
    void importsFoodSafetyRecipes() throws Exception {
        FoodSafetyRecipeDto response = response(row("1001", "chicken salad"));
        when(foodSafetyRecipeClient.fetch(1, 1)).thenReturn(response);
        when(recipeRepository.existsBySourceAndExternalId("FOOD_SAFETY_KOREA", "1001")).thenReturn(false);
        when(recipeIngredientFoodMatcher.matchFoodId("chicken breast")).thenReturn(1L);
        when(recipeRepository.save(any(Recipe.class))).thenAnswer(invocation -> invocation.getArgument(0));

        RecipeImportService.ImportResult result = recipeImportService.importFoodSafetyRecipes(1, 1);

        assertThat(result.fetched()).isEqualTo(1);
        assertThat(result.imported()).isEqualTo(1);
        assertThat(result.skipped()).isZero();

        ArgumentCaptor<Recipe> recipeCaptor = ArgumentCaptor.forClass(Recipe.class);
        verify(recipeRepository).save(recipeCaptor.capture());

        Recipe saved = recipeCaptor.getValue();
        assertThat(saved.getExternalId()).isEqualTo("1001");
        assertThat(saved.getSource()).isEqualTo("FOOD_SAFETY_KOREA");
        assertThat(saved.getTitle()).isEqualTo("chicken salad");
        assertThat(saved.getTotalCalories()).isEqualByComparingTo("350");
        assertThat(saved.getIngredients()).hasSize(2);
        assertThat(saved.getIngredients().get(0).getFoodId()).isEqualTo(1L);
        assertThat(saved.getIngredients().get(0).getIngredientName()).isEqualTo("chicken breast");
        assertThat(saved.getIngredients().get(0).getQuantity()).isEqualByComparingTo("100");
        assertThat(saved.getIngredients().get(0).getUnit()).isEqualTo("g");
        assertThat(saved.getSteps()).hasSize(2);
    }

    @Test
    void skipsAlreadyImportedFoodSafetyRecipes() throws Exception {
        FoodSafetyRecipeDto response = response(row("1001", "chicken salad"));
        when(foodSafetyRecipeClient.fetch(1, 1)).thenReturn(response);
        when(recipeRepository.existsBySourceAndExternalId("FOOD_SAFETY_KOREA", "1001")).thenReturn(true);

        RecipeImportService.ImportResult result = recipeImportService.importFoodSafetyRecipes(1, 1);

        assertThat(result.fetched()).isEqualTo(1);
        assertThat(result.imported()).isZero();
        assertThat(result.skipped()).isEqualTo(1);
        verify(recipeRepository, never()).save(any());
    }

    private FoodSafetyRecipeDto response(FoodSafetyRecipeDto.Row... rows) throws Exception {
        FoodSafetyRecipeDto dto = new FoodSafetyRecipeDto();
        FoodSafetyRecipeDto.CookRecipe cookRecipe = new FoodSafetyRecipeDto.CookRecipe();
        cookRecipe.setRow(List.of(rows));

        Field field = FoodSafetyRecipeDto.class.getDeclaredField("cookRecipe");
        field.setAccessible(true);
        field.set(dto, cookRecipe);

        return dto;
    }

    private FoodSafetyRecipeDto.Row row(String recipeSeq, String recipeName) {
        FoodSafetyRecipeDto.Row row = new FoodSafetyRecipeDto.Row();
        row.put("RCP_SEQ", recipeSeq);
        row.put("RCP_NM", recipeName);
        row.put("RCP_PAT2", "salad");
        row.put("RCP_WAY2", "etc");
        row.put("INFO_ENG", "350");
        row.put("INFO_CAR", "10");
        row.put("INFO_PRO", "25");
        row.put("INFO_FAT", "8");
        row.put("INFO_NA", "300");
        row.put("ATT_FILE_NO_MAIN", "https://example.com/main.jpg");
        row.put("RCP_PARTS_DTLS", "chicken breast 100g lettuce 50g");
        row.put("MANUAL01", "prepare ingredients");
        row.put("MANUAL02", "serve in a bowl");
        return row;
    }
}
