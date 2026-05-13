package com.meta.foremeal.recipe.service;

import com.meta.foremeal.recipe.domain.Recipe;
import com.meta.foremeal.recipe.domain.RecipeIngredient;
import com.meta.foremeal.recipe.domain.RecipeStep;
import com.meta.foremeal.recipe.domain.Substitute;
import com.meta.foremeal.recipe.dto.RecipeDto;
import com.meta.foremeal.recipe.repo.RecipeRepository;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class RecipeServiceTest {

    private final RecipeRepository recipeRepository = mock(RecipeRepository.class);
    private final RecipeService recipeService = new RecipeService(recipeRepository);

    @Test
    void createsRecipeWithIngredientsStepsAndSubstitutes() {
        RecipeDto.CreateRequest request = createRequest("닭가슴살 샐러드", "샐러드", "기타", "EASY", 15);
        when(recipeRepository.save(any(Recipe.class))).thenAnswer(invocation -> invocation.getArgument(0));

        RecipeDto.Response response = recipeService.create(request);

        assertThat(response.title()).isEqualTo("닭가슴살 샐러드");
        assertThat(response.category()).isEqualTo("샐러드");
        assertThat(response.dishType()).isEqualTo("기타");
        assertThat(response.difficulty()).isEqualTo("EASY");
        assertThat(response.imageUri()).isEqualTo("https://example.com/main.jpg");
        assertThat(response.ingredients()).hasSize(1);
        assertThat(response.ingredients().get(0).ingredientName()).isEqualTo("닭가슴살");
        assertThat(response.ingredients().get(0).substitutes()).hasSize(1);
        assertThat(response.ingredients().get(0).substitutes().get(0).description()).isEqualTo("두부 100g으로 대체 가능");
        assertThat(response.steps()).extracting(RecipeDto.StepResponse::stepOrder).containsExactly(1, 2);
    }

    @Test
    void getsRecipeByIdWithNestedData() {
        Recipe recipe = recipeServiceEntity("닭가슴살 샐러드", "샐러드", "기타", "EASY", 15);
        when(recipeRepository.findById(1L)).thenReturn(Optional.of(recipe));

        RecipeDto.Response response = recipeService.getById(1L);

        assertThat(response.title()).isEqualTo("닭가슴살 샐러드");
        assertThat(response.ingredients()).hasSize(1);
        assertThat(response.ingredients().get(0).substitutes()).hasSize(1);
        assertThat(response.steps()).hasSize(2);
    }

    @Test
    void filtersRecipes() {
        Recipe matched = recipeServiceEntity("닭가슴살 샐러드", "샐러드", "기타", "EASY", 15);
        Recipe wrongCategory = recipeServiceEntity("현미밥", "밥", "끓이기", "EASY", 10);
        Recipe tooSlow = recipeServiceEntity("오븐구이", "샐러드", "기타", "EASY", 40);
        when(recipeRepository.findAll()).thenReturn(List.of(matched, wrongCategory, tooSlow));

        List<RecipeDto.Response> responses = recipeService.getAll("샐러드", "기타", "EASY", 20);

        assertThat(responses).hasSize(1);
        assertThat(responses.get(0).title()).isEqualTo("닭가슴살 샐러드");
    }

    private RecipeDto.CreateRequest createRequest(String title, String category, String dishType,
                                                  String difficulty, Integer cookingTime) {
        return new RecipeDto.CreateRequest(
                title,
                "가벼운 식단",
                category,
                dishType,
                difficulty,
                cookingTime,
                1,
                new BigDecimal("350"),
                "{\"carbs\":10,\"protein\":25}",
                "LOW",
                "https://example.com/main.jpg",
                List.of(new RecipeDto.IngredientRequest(
                        1L,
                        "닭가슴살",
                        new BigDecimal("100"),
                        "g",
                        List.of(new RecipeDto.SubstituteRequest(
                                BigDecimal.ONE,
                                "두부 100g으로 대체 가능"
                        ))
                )),
                List.of(
                        new RecipeDto.StepRequest(2, "그릇에 담는다.", null),
                        new RecipeDto.StepRequest(1, "재료를 손질한다.", null)
                )
        );
    }

    private Recipe recipeServiceEntity(String title, String category, String dishType,
                                       String difficulty, Integer cookingTime) {
        Recipe recipe = new Recipe(
                title,
                "가벼운 식단",
                category,
                dishType,
                difficulty,
                cookingTime,
                1,
                new BigDecimal("350"),
                "{\"carbs\":10,\"protein\":25}",
                "LOW",
                "https://example.com/main.jpg"
        );

        RecipeIngredient ingredient = new RecipeIngredient(1L, "닭가슴살", new BigDecimal("100"), "g");
        ingredient.addSubstitute(new Substitute(BigDecimal.ONE, "두부 100g으로 대체 가능"));
        recipe.addIngredient(ingredient);

        recipe.addStep(new RecipeStep(2, "그릇에 담는다.", null));
        recipe.addStep(new RecipeStep(1, "재료를 손질한다.", null));

        return recipe;
    }
}
