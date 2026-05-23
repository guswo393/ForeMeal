package com.meta.foremeal.recipe.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.meta.foremeal.foodmaster.domain.FoodMasterEntity;
import com.meta.foremeal.foodmaster.repo.FoodMasterRepository;
import com.meta.foremeal.recipe.domain.Recipe;
import com.meta.foremeal.recipe.domain.RecipeIngredient;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class RecipeNutritionCalculator {

    private final FoodMasterRepository foodMasterRepository;
    private final ObjectMapper objectMapper;

    public RecipeNutritionCalculator(FoodMasterRepository foodMasterRepository, ObjectMapper objectMapper) {
        this.foodMasterRepository = foodMasterRepository;
        this.objectMapper = objectMapper;
    }

    public Result calculate(Recipe recipe) {
        Map<String, Double> nutrients = parseNutrients(recipe.getTotalNutrients());
        Map<String, Double> estimated = estimateNutrientsFromIngredients(recipe);

        estimated.forEach((key, value) -> {
            if (value != null && value > 0.0 && !nutrients.containsKey(key)) {
                nutrients.put(key, value);
            }
        });

        BigDecimal calories = recipe.getTotalCalories();
        if ((calories == null || calories.signum() <= 0) && estimated.containsKey("calories")) {
            calories = BigDecimal.valueOf(estimated.get("calories"));
        }

        return new Result(calories, toJson(nutrients), nutrients);
    }

    public double nutrient(Recipe recipe, String key) {
        return calculate(recipe).nutrients().getOrDefault(key, 0.0);
    }

    private String toJson(Map<String, Double> nutrients) {
        try {
            return objectMapper.writeValueAsString(nutrients);
        } catch (Exception e) {
            return "{}";
        }
    }

    private Map<String, Double> parseNutrients(String totalNutrients) {
        Map<String, Double> nutrients = new LinkedHashMap<>();
        if (!StringUtils.hasText(totalNutrients)) {
            return nutrients;
        }

        try {
            JsonNode node = objectMapper.readTree(totalNutrients);
            addNutrient(nutrients, "carbs", node, "carbs", "CARBS", "carbohydrate");
            addNutrient(nutrients, "protein", node, "protein", "PROTEIN");
            addNutrient(nutrients, "fat", node, "fat", "FAT");
            addNutrient(nutrients, "sugar", node, "sugar", "SUGAR", "sugars");
            addNutrient(nutrients, "sodium", node, "sodium", "SODIUM", "na", "NA");
        } catch (Exception ignored) {
            return nutrients;
        }

        return nutrients;
    }

    private void addNutrient(Map<String, Double> nutrients, String targetKey, JsonNode node, String... sourceKeys) {
        for (String sourceKey : sourceKeys) {
            JsonNode value = node.get(sourceKey);
            if (value != null && value.isNumber()) {
                nutrients.put(targetKey, roundOne(value.doubleValue()));
                return;
            }
        }
    }

    private Map<String, Double> estimateNutrientsFromIngredients(Recipe recipe) {
        List<Long> foodIds = recipe.getIngredients().stream()
                .map(RecipeIngredient::getFoodId)
                .filter(foodId -> foodId != null)
                .distinct()
                .toList();

        List<FoodMasterEntity> foods = foodIds.isEmpty() ? List.of() : foodMasterRepository.findAllById(foodIds);
        if (foods == null) {
            foods = List.of();
        }

        Map<Long, FoodMasterEntity> foodsById = foods.stream()
                .collect(Collectors.toMap(FoodMasterEntity::getFoodId, food -> food));

        Map<String, Double> totals = new LinkedHashMap<>();
        for (RecipeIngredient ingredient : recipe.getIngredients()) {
            FoodMasterEntity food = ingredient.getFoodId() == null ? null : foodsById.get(ingredient.getFoodId());
            List<FoodMasterEntity> matchedFoods = food == null
                    ? findFoodsByIngredientName(ingredient.getIngredientName())
                    : List.of(food);

            if (matchedFoods.isEmpty()) {
                continue;
            }

            double factor = ingredientFactor(ingredient);
            for (FoodMasterEntity matchedFood : matchedFoods) {
                addEstimatedNutrient(totals, "calories", matchedFood.getCalories(), factor);
                addEstimatedNutrient(totals, "carbs", matchedFood.getCarbs(), factor);
                addEstimatedNutrient(totals, "protein", matchedFood.getProtein(), factor);
                addEstimatedNutrient(totals, "fat", matchedFood.getFat(), factor);
                addEstimatedNutrient(totals, "sugar", matchedFood.getSugar(), factor);
                addEstimatedNutrient(totals, "sodium", matchedFood.getSodium(), factor);
            }
        }

        totals.replaceAll((key, value) -> roundOne(value));
        return totals;
    }

    private List<FoodMasterEntity> findFoodsByIngredientName(String ingredientName) {
        List<FoodMasterEntity> matchedFoods = new ArrayList<>();

        for (String ingredientPart : splitIngredientName(ingredientName)) {
            addFoodIfPresent(matchedFoods, findFoodByName(ingredientPart));

            for (String token : ingredientSearchTokens(ingredientPart)) {
                addFoodIfPresent(matchedFoods, findFoodByName(token));
            }
        }

        return matchedFoods;
    }

    private void addFoodIfPresent(List<FoodMasterEntity> foods, FoodMasterEntity food) {
        if (food == null || food.getFoodId() == null) {
            return;
        }

        boolean alreadyAdded = foods.stream()
                .anyMatch(existing -> existing.getFoodId().equals(food.getFoodId()));
        if (!alreadyAdded) {
            foods.add(food);
        }
    }

    private List<String> splitIngredientName(String ingredientName) {
        if (!StringUtils.hasText(ingredientName)) {
            return List.of();
        }

        String normalized = ingredientName
                .replace("\r", "\n")
                .replace("ㆍ", ",")
                .replace("·", ",")
                .replace("，", ",")
                .replace(";", ",");

        List<String> parts = new ArrayList<>();
        for (String rawPart : normalized.split("[,\\n]+")) {
            String cleaned = cleanIngredientPart(rawPart);
            if (StringUtils.hasText(cleaned) && !parts.contains(cleaned)) {
                parts.add(cleaned);
            }
        }

        if (parts.isEmpty()) {
            String cleaned = cleanIngredientPart(ingredientName);
            if (StringUtils.hasText(cleaned)) {
                parts.add(cleaned);
            }
        }

        return parts;
    }

    private String cleanIngredientPart(String value) {
        if (!StringUtils.hasText(value)) {
            return null;
        }

        String cleaned = value.replaceAll("^[^:：]*[:：]\\s*", "")
                .replaceAll("\\([^)]*\\)", " ")
                .replaceAll("\\d+(?:\\.\\d+)?(?:/\\d+)?\\s*(?:g|kg|ml|l|개|장|쪽|알|큰술|작은술|컵|cm|T|t)", " ")
                .replaceAll("\\d+(?:\\.\\d+)?(?:/\\d+)?", " ")
                .replaceAll("[\\[\\]{}()]", " ")
                .replaceAll("\\s+", " ")
                .trim();

        return StringUtils.hasText(cleaned) ? cleaned : null;
    }

    private List<String> ingredientSearchTokens(String ingredientName) {
        String cleaned = cleanIngredientPart(ingredientName);
        if (!StringUtils.hasText(cleaned)) {
            return List.of();
        }

        List<String> tokens = new ArrayList<>();
        for (String token : cleaned.split("\\s+")) {
            String normalized = token
                    .replaceAll("(으로|로|와|과|은|는|이|가|을|를)$", "")
                    .trim();
            if (normalized.length() >= 2 && !tokens.contains(normalized)) {
                tokens.add(normalized);
            }
        }

        return tokens;
    }

    private FoodMasterEntity findFoodByName(String ingredientName) {
        String normalizedIngredient = normalizeName(ingredientName);
        if (normalizedIngredient == null) {
            return null;
        }

        return foodMasterRepository.findByFoodNameContainingIgnoreCase(ingredientName)
                .stream()
                .filter(food -> {
                    String normalizedFoodName = normalizeName(food.getFoodName());
                    return normalizedFoodName != null
                            && (normalizedFoodName.equals(normalizedIngredient)
                            || normalizedFoodName.contains(normalizedIngredient)
                            || normalizedIngredient.contains(normalizedFoodName));
                })
                .min(Comparator.comparingInt(food -> food.getFoodName().length()))
                .orElse(null);
    }

    private double ingredientFactor(RecipeIngredient ingredient) {
        if (ingredient.getQuantity() == null || ingredient.getQuantity().signum() <= 0) {
            return 1.0;
        }

        double quantity = ingredient.getQuantity().doubleValue();
        String unit = ingredient.getUnit() == null ? "" : ingredient.getUnit().toLowerCase(Locale.ROOT);

        if (unit.contains("kg")) {
            return quantity * 10.0;
        }
        if (unit.contains("g") || unit.contains("그램") || unit.contains("ml")) {
            return quantity / 100.0;
        }
        if ("l".equals(unit) || unit.contains("리터")) {
            return quantity * 10.0;
        }

        return Math.max(1.0, quantity);
    }

    private void addEstimatedNutrient(Map<String, Double> totals, String key, Double valuePer100g, double factor) {
        if (valuePer100g == null || valuePer100g <= 0.0) {
            return;
        }

        totals.merge(key, valuePer100g * factor, Double::sum);
    }

    private double roundOne(double value) {
        return Math.round(value * 10.0) / 10.0;
    }

    private String normalizeName(String value) {
        if (!StringUtils.hasText(value)) {
            return null;
        }
        return value.replaceAll("\\s+", "").toLowerCase(Locale.ROOT);
    }

    public record Result(
            BigDecimal calories,
            String nutrientsJson,
            Map<String, Double> nutrients
    ) {
    }
}
