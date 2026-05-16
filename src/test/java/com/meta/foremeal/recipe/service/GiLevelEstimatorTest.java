package com.meta.foremeal.recipe.service;

import com.meta.foremeal.foodmaster.domain.FoodMasterEntity;
import com.meta.foremeal.foodmaster.repo.FoodMasterRepository;
import com.meta.foremeal.recipe.domain.RecipeIngredient;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class GiLevelEstimatorTest {

    private final FoodMasterRepository foodRepository = mock(FoodMasterRepository.class);
    private final GiLevelEstimator estimator = new GiLevelEstimator(foodRepository);

    @Test
    void returnsLowForLowWeightedAverageGi() {
        RecipeIngredient brownRice = ingredient(1L, "brown rice", "120");
        RecipeIngredient chicken = ingredient(2L, "chicken breast", "80");

        when(foodRepository.findAllById(List.of(1L, 2L)))
                .thenReturn(List.of(food(1L, 52.0), food(2L, 0.0)));

        assertThat(estimator.estimate(List.of(brownRice, chicken))).isEqualTo("LOW");
    }

    @Test
    void returnsMediumForMediumWeightedAverageGi() {
        RecipeIngredient rice = ingredient(1L, "rice", "100");
        RecipeIngredient bean = ingredient(2L, "bean", "50");

        when(foodRepository.findAllById(List.of(1L, 2L)))
                .thenReturn(List.of(food(1L, 72.0), food(2L, 45.0)));

        assertThat(estimator.estimate(List.of(rice, bean))).isEqualTo("MEDIUM");
    }

    @Test
    void returnsHighForHighWeightedAverageGi() {
        RecipeIngredient riceCake = ingredient(1L, "rice cake", "150");

        when(foodRepository.findAllById(List.of(1L)))
                .thenReturn(List.of(food(1L, 82.0)));

        assertThat(estimator.estimate(List.of(riceCake))).isEqualTo("HIGH");
    }

    @Test
    void returnsNullWhenNoMatchedFoodHasGiIndex() {
        RecipeIngredient unknown = ingredient(1L, "unknown", "100");

        when(foodRepository.findAllById(List.of(1L)))
                .thenReturn(List.of(food(1L, null)));

        assertThat(estimator.estimate(List.of(unknown))).isNull();
    }

    private RecipeIngredient ingredient(Long foodId, String name, String quantity) {
        return new RecipeIngredient(foodId, name, new BigDecimal(quantity), "g");
    }

    private FoodMasterEntity food(Long foodId, Double giIndex) {
        FoodMasterEntity food = new FoodMasterEntity();
        food.setFoodId(foodId);
        food.setFoodName("food-" + foodId);
        food.setGiIndex(giIndex);
        return food;
    }
}
