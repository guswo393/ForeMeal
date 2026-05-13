package com.meta.foremeal.recipe.service;

import com.meta.foremeal.foodmaster.domain.FoodMasterEntity;
import com.meta.foremeal.foodmaster.repo.FoodMasterRepository;
import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.List;
import java.util.Locale;

@Component
public class RecipeIngredientFoodMatcher {

    private final FoodMasterRepository foodRepository;

    public RecipeIngredientFoodMatcher(FoodMasterRepository foodRepository) {
        this.foodRepository = foodRepository;
    }

    public Long matchFoodId(String ingredientName) {
        if (ingredientName == null || ingredientName.isBlank()) {
            return null;
        }

        List<FoodMasterEntity> candidates = foodRepository.findByFoodNameContainingIgnoreCase(ingredientName.trim());
        if (candidates.isEmpty()) {
            return null;
        }

        String normalizedIngredient = normalize(ingredientName);

        return candidates.stream()
                .min(Comparator
                        .comparing((FoodMasterEntity food) -> exactMatchRank(normalizedIngredient, food))
                        .thenComparing(food -> food.getFoodName() == null ? Integer.MAX_VALUE : food.getFoodName().length())
                        .thenComparing(FoodMasterEntity::getFoodId, Comparator.nullsLast(Long::compareTo)))
                .map(FoodMasterEntity::getFoodId)
                .orElse(null);
    }

    private int exactMatchRank(String normalizedIngredient, FoodMasterEntity food) {
        return normalize(food.getFoodName()).equals(normalizedIngredient) ? 0 : 1;
    }

    private String normalize(String value) {
        return value == null
                ? ""
                : value.replaceAll("\\s+", "").toLowerCase(Locale.ROOT);
    }
}
