package com.meta.foremeal.recipe.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.meta.foremeal.foodmaster.domain.FoodMasterEntity;
import com.meta.foremeal.foodmaster.repo.FoodMasterRepository;
import com.meta.foremeal.recipe.domain.Recipe;
import com.meta.foremeal.recipe.domain.RecipeIngredient;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class RecipeNutritionCalculatorTest {

    private final FoodMasterRepository foodMasterRepository = mock(FoodMasterRepository.class);
    private final RecipeNutritionCalculator calculator = new RecipeNutritionCalculator(
            foodMasterRepository,
            new ObjectMapper()
    );

    @Test
    void fillsMissingSugarFromIngredientNamesWithoutFoodIds() {
        FoodMasterEntity apple = food(1L, "사과", 52.0, 13.8, 0.3, 0.2, 10.4, 1.0);
        FoodMasterEntity shrimp = food(2L, "새우", 99.0, 0.2, 24.0, 0.3, 0.0, 111.0);

        when(foodMasterRepository.findByFoodNameContainingIgnoreCase("사과와 새우 북엇국"))
                .thenReturn(List.of());
        when(foodMasterRepository.findByFoodNameContainingIgnoreCase("사과"))
                .thenReturn(List.of(apple));
        when(foodMasterRepository.findByFoodNameContainingIgnoreCase("새우"))
                .thenReturn(List.of(shrimp));
        when(foodMasterRepository.findByFoodNameContainingIgnoreCase("북엇국"))
                .thenReturn(List.of());

        Recipe recipe = recipe("{\"carbs\":2,\"protein\":12,\"sodium\":78}");
        recipe.addIngredient(new RecipeIngredient(null, "사과와 새우 북엇국", null, null));

        RecipeNutritionCalculator.Result result = calculator.calculate(recipe);

        assertThat(result.nutrients()).containsEntry("carbs", 2.0);
        assertThat(result.nutrients()).containsEntry("protein", 12.0);
        assertThat(result.nutrients()).containsEntry("sodium", 78.0);
        assertThat(result.nutrients()).containsEntry("sugar", 10.4);
        assertThat(result.source()).isEqualTo("MIXED");
        assertThat(result.confidence()).isGreaterThan(0.0);
        assertThat(result.warnings()).anyMatch(warning -> warning.contains("추정"));
    }

    @Test
    void usesFoodIdWhenIngredientHasFoodId() {
        FoodMasterEntity banana = food(5L, "바나나", 89.0, 22.8, 1.1, 0.3, 12.2, 1.0);
        when(foodMasterRepository.findAllById(List.of(5L))).thenReturn(List.of(banana));

        Recipe recipe = recipe("{}");
        recipe.addIngredient(new RecipeIngredient(5L, "바나나", new BigDecimal("100"), "g"));

        RecipeNutritionCalculator.Result result = calculator.calculate(recipe);

        assertThat(result.calories()).isEqualByComparingTo("89.0");
        assertThat(result.nutrients()).containsEntry("carbs", 22.8);
        assertThat(result.nutrients()).containsEntry("sugar", 12.2);
        assertThat(result.nutrients()).containsEntry("sodium", 1.0);
        assertThat(result.source()).isEqualTo("FOOD_ID_CALCULATED");
        assertThat(result.confidence()).isEqualTo(0.9);
    }

    @Test
    void convertsPieceUnitsWithIngredientAliases() {
        FoodMasterEntity egg = food(7L, "달걀", 155.0, 1.1, 13.0, 11.0, 1.1, 124.0);
        when(foodMasterRepository.findByFoodNameContainingIgnoreCase("계란"))
                .thenReturn(List.of());
        when(foodMasterRepository.findByFoodNameContainingIgnoreCase("달걀"))
                .thenReturn(List.of(egg));

        Recipe recipe = recipe("{}");
        recipe.addIngredient(new RecipeIngredient(null, "계란", new BigDecimal("2"), "개"));

        RecipeNutritionCalculator.Result result = calculator.calculate(recipe);

        assertThat(result.calories()).isEqualByComparingTo("155.0");
        assertThat(result.nutrients()).containsEntry("protein", 13.0);
        assertThat(result.nutrients()).containsEntry("sugar", 1.1);
        assertThat(result.source()).isEqualTo("NAME_ESTIMATED");
        assertThat(result.warnings()).anyMatch(warning -> warning.contains("평균 중량"));
    }

    private Recipe recipe(String nutrients) {
        return new Recipe(
                "테스트 레시피",
                "설명",
                "분류",
                "방식",
                "EASY",
                5,
                1,
                null,
                nutrients,
                "LOW",
                null
        );
    }

    private FoodMasterEntity food(Long id, String name, Double calories, Double carbs, Double protein,
                                  Double fat, Double sugar, Double sodium) {
        FoodMasterEntity food = new FoodMasterEntity();
        food.setFoodId(id);
        food.setFoodName(name);
        food.setCalories(calories);
        food.setCarbs(carbs);
        food.setProtein(protein);
        food.setFat(fat);
        food.setSugar(sugar);
        food.setSodium(sodium);
        return food;
    }
}
