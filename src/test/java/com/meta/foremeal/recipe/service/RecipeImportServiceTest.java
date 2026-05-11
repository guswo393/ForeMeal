package com.meta.foremeal.recipe.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.meta.foremeal.recipe.domain.Recipe;
import com.meta.foremeal.recipe.external.FoodSafetyRecipeClient;
import com.meta.foremeal.recipe.external.FoodSafetyRecipeDto;
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
    private final RecipeImportService recipeImportService = new RecipeImportService(
            foodSafetyRecipeClient,
            recipeRepository,
            new ObjectMapper()
    );

    @Test
    void importsFoodSafetyRecipes() throws Exception {
        FoodSafetyRecipeDto response = response(row("1001", "닭가슴살 샐러드"));
        when(foodSafetyRecipeClient.fetch(1, 1)).thenReturn(response);
        when(recipeRepository.existsBySourceAndExternalId("FOOD_SAFETY_KOREA", "1001")).thenReturn(false);
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
        assertThat(saved.getTitle()).isEqualTo("닭가슴살 샐러드");
        assertThat(saved.getTotalCalories()).isEqualByComparingTo("350");
        assertThat(saved.getIngredients()).hasSize(1);
        assertThat(saved.getSteps()).hasSize(2);
    }

    @Test
    void skipsAlreadyImportedFoodSafetyRecipes() throws Exception {
        FoodSafetyRecipeDto response = response(row("1001", "닭가슴살 샐러드"));
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
        row.put("RCP_PAT2", "샐러드");
        row.put("RCP_WAY2", "기타");
        row.put("INFO_ENG", "350");
        row.put("INFO_CAR", "10");
        row.put("INFO_PRO", "25");
        row.put("INFO_FAT", "8");
        row.put("INFO_NA", "300");
        row.put("ATT_FILE_NO_MAIN", "https://example.com/main.jpg");
        row.put("RCP_PARTS_DTLS", "닭가슴살 100g 양상추 50g");
        row.put("MANUAL01", "재료를 손질한다.");
        row.put("MANUAL02", "그릇에 담는다.");
        return row;
    }
}
