package com.meta.foremeal.recipe.service;

import com.meta.foremeal.foodmaster.domain.FoodMasterEntity;
import com.meta.foremeal.foodmaster.repo.FoodMasterRepository;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class RecipeIngredientFoodMatcherTest {

    private final FoodMasterRepository foodRepository = mock(FoodMasterRepository.class);
    private final RecipeIngredientFoodMatcher matcher = new RecipeIngredientFoodMatcher(foodRepository);

    @Test
    void returnsMatchedFoodIdByIngredientName() {
        when(foodRepository.findByFoodNameContainingIgnoreCase("닭가슴살"))
                .thenReturn(List.of(food(1L, "닭가슴살"), food(2L, "훈제 닭가슴살")));

        Long foodId = matcher.matchFoodId("닭가슴살");

        assertThat(foodId).isEqualTo(1L);
    }

    @Test
    void returnsNullWhenNoFoodMatches() {
        when(foodRepository.findByFoodNameContainingIgnoreCase("알수없는재료"))
                .thenReturn(List.of());

        Long foodId = matcher.matchFoodId("알수없는재료");

        assertThat(foodId).isNull();
    }

    private FoodMasterEntity food(Long foodId, String name) {
        FoodMasterEntity food = new FoodMasterEntity();
        food.setFoodId(foodId);
        food.setFoodName(name);
        return food;
    }
}
