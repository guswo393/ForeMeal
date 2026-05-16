package com.meta.foremeal.recipe.service;

import com.meta.foremeal.foodmaster.domain.FoodMasterEntity;
import com.meta.foremeal.foodmaster.repo.FoodMasterRepository;
import com.meta.foremeal.recipe.domain.RecipeIngredient;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
public class GiLevelEstimator {

    private static final BigDecimal LOW_GI_MAX = new BigDecimal("55");
    private static final BigDecimal MEDIUM_GI_MAX = new BigDecimal("69");

    private final FoodMasterRepository foodRepository;

    public GiLevelEstimator(FoodMasterRepository foodRepository) {
        this.foodRepository = foodRepository;
    }

    public String estimate(List<RecipeIngredient> ingredients) {
        if (ingredients == null || ingredients.isEmpty()) {
            return null;
        }

        List<Long> foodIds = ingredients.stream()
                .map(RecipeIngredient::getFoodId)
                .filter(foodId -> foodId != null)
                .distinct()
                .toList();

        if (foodIds.isEmpty()) {
            return null;
        }

        Map<Long, FoodMasterEntity> foodsById = foodRepository.findAllById(foodIds)
                .stream()
                .collect(Collectors.toMap(
                        FoodMasterEntity::getFoodId,
                        Function.identity()
                ));

        BigDecimal weightedGiSum = BigDecimal.ZERO;
        BigDecimal totalWeight = BigDecimal.ZERO;

        for (RecipeIngredient ingredient : ingredients) {
            Long foodId = ingredient.getFoodId();
            if (foodId == null) {
                continue;
            }

            FoodMasterEntity food = foodsById.get(foodId);
            if (food == null || food.getGiIndex() == null) {
                continue;
            }

            BigDecimal weight = ingredient.getQuantity() == null || ingredient.getQuantity().signum() <= 0
                    ? BigDecimal.ONE
                    : ingredient.getQuantity();
            BigDecimal giIndex = BigDecimal.valueOf(food.getGiIndex());

            weightedGiSum = weightedGiSum.add(giIndex.multiply(weight));
            totalWeight = totalWeight.add(weight);
        }

        if (totalWeight.signum() == 0) {
            return null;
        }

        BigDecimal averageGi = weightedGiSum.divide(totalWeight, 2, RoundingMode.HALF_UP);
        if (averageGi.compareTo(LOW_GI_MAX) <= 0) {
            return "LOW";
        }
        if (averageGi.compareTo(MEDIUM_GI_MAX) <= 0) {
            return "MEDIUM";
        }
        return "HIGH";
    }
}
