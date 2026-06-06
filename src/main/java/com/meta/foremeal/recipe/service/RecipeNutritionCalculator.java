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
        BigDecimal calories = recipe.getTotalCalories();
        if (hasUsableOriginalNutrition(nutrients, calories)) {
            return new Result(calories, toJson(nutrients), nutrients, Source.ORIGINAL.name(), 0.85, List.of());
        }

        Estimation estimation = estimateNutrientsFromIngredients(recipe);
        Map<String, Double> estimated = estimation.nutrients();
        List<String> warnings = new ArrayList<>(estimation.warnings());
        boolean hasOriginal = !nutrients.isEmpty() || recipe.getTotalCalories() != null;
        boolean usedEstimated = false;

        estimated.forEach((key, value) -> {
            if (value != null && value > 0.0 && !nutrients.containsKey(key)) {
                nutrients.put(key, value);
            }
        });
        usedEstimated = estimated.entrySet().stream()
                .anyMatch(entry -> entry.getValue() != null && entry.getValue() > 0.0);

        if ((calories == null || calories.signum() <= 0) && estimated.containsKey("calories")) {
            calories = BigDecimal.valueOf(estimated.get("calories"));
            usedEstimated = true;
        }

        Source source = resolveSource(hasOriginal, estimation.usedFoodId(), estimation.usedNameMatch(), nutrients);
        double confidence = confidence(source, estimation, nutrients, calories, usedEstimated);
        if (source == Source.MISSING) {
            warnings.add("계산 가능한 영양정보가 없습니다.");
        } else if (source == Source.NAME_ESTIMATED || source == Source.MIXED) {
            warnings.add("일부 영양값은 재료명 매칭으로 추정되었습니다.");
        }

        return new Result(calories, toJson(nutrients), nutrients, source.name(), confidence, warnings);
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

    private boolean hasUsableOriginalNutrition(Map<String, Double> nutrients, BigDecimal calories) {
        return calories != null
                && calories.signum() > 0
                && nutrients.containsKey("carbs")
                && nutrients.containsKey("sugar")
                && nutrients.containsKey("sodium");
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

    private Estimation estimateNutrientsFromIngredients(Recipe recipe) {
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
        List<String> warnings = new ArrayList<>();
        int ingredientCount = 0;
        int matchedCount = 0;
        boolean usedFoodId = false;
        boolean usedNameMatch = false;

        for (RecipeIngredient ingredient : recipe.getIngredients()) {
            ingredientCount++;
            FoodMasterEntity food = ingredient.getFoodId() == null ? null : foodsById.get(ingredient.getFoodId());
            boolean matchedByFoodId = food != null;
            List<FoodMasterEntity> matchedFoods = food == null
                    ? findFoodsByIngredientName(ingredient.getIngredientName())
                    : List.of(food);

            if (matchedFoods.isEmpty()) {
                warnings.add("식품 DB와 매칭되지 않은 재료가 있습니다: " + ingredient.getIngredientName());
                continue;
            }

            matchedCount++;
            usedFoodId = usedFoodId || matchedByFoodId;
            usedNameMatch = usedNameMatch || !matchedByFoodId;

            Factor factor = ingredientFactor(ingredient);
            warnings.addAll(factor.warnings());
            for (FoodMasterEntity matchedFood : matchedFoods) {
                addEstimatedNutrient(totals, "calories", matchedFood.getCalories(), factor.multiplier());
                addEstimatedNutrient(totals, "carbs", matchedFood.getCarbs(), factor.multiplier());
                addEstimatedNutrient(totals, "protein", matchedFood.getProtein(), factor.multiplier());
                addEstimatedNutrient(totals, "fat", matchedFood.getFat(), factor.multiplier());
                addEstimatedNutrient(totals, "sugar", matchedFood.getSugar(), factor.multiplier());
                addEstimatedNutrient(totals, "sodium", matchedFood.getSodium(), factor.multiplier());
            }
        }

        totals.replaceAll((key, value) -> roundOne(value));
        double matchRate = ingredientCount == 0 ? 0.0 : (double) matchedCount / ingredientCount;
        return new Estimation(totals, usedFoodId, usedNameMatch, matchRate, warnings);
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
                aliasTokens(normalized).forEach(alias -> {
                    if (!tokens.contains(alias)) {
                        tokens.add(alias);
                    }
                });
            }
        }

        return tokens;
    }

    private List<String> aliasTokens(String token) {
        return switch (token) {
            case "계란" -> List.of("달걀");
            case "달걀" -> List.of("계란");
            case "대파", "쪽파" -> List.of(token);
            case "파" -> List.of("대파", "쪽파");
            case "돼지고기" -> List.of("고기");
            case "쇠고기" -> List.of("소고기");
            default -> List.of();
        };
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
                    if (normalizedFoodName == null) {
                        return false;
                    }
                    if (normalizedIngredient.length() < 2 || normalizedFoodName.length() < 2) {
                        return normalizedFoodName.equals(normalizedIngredient);
                    }
                    return normalizedFoodName != null
                            && (normalizedFoodName.equals(normalizedIngredient)
                            || normalizedFoodName.contains(normalizedIngredient)
                            || normalizedIngredient.contains(normalizedFoodName));
                })
                .min(Comparator.comparingInt(food -> food.getFoodName().length()))
                .orElse(null);
    }

    private Factor ingredientFactor(RecipeIngredient ingredient) {
        List<String> warnings = new ArrayList<>();
        if (ingredient.getQuantity() == null || ingredient.getQuantity().signum() <= 0) {
            warnings.add("수량이 없는 재료는 1회 제공량 기준으로 추정했습니다: " + ingredient.getIngredientName());
            return new Factor(1.0, warnings);
        }

        double quantity = ingredient.getQuantity().doubleValue();
        String unit = ingredient.getUnit() == null ? "" : ingredient.getUnit().toLowerCase(Locale.ROOT);

        if (unit.contains("kg")) {
            return new Factor(quantity * 10.0, warnings);
        }
        if (unit.contains("g") || unit.contains("그램") || unit.contains("ml")) {
            return new Factor(quantity / 100.0, warnings);
        }
        if ("l".equals(unit) || unit.contains("리터")) {
            return new Factor(quantity * 10.0, warnings);
        }
        if (unit.contains("큰술") || "t".equals(unit) || unit.contains("tbsp")) {
            return new Factor(quantity * 15.0 / 100.0, warnings);
        }
        if (unit.contains("작은술") || "tsp".equals(unit)) {
            return new Factor(quantity * 5.0 / 100.0, warnings);
        }
        if (unit.contains("컵")) {
            return new Factor(quantity * 200.0 / 100.0, warnings);
        }
        if (unit.contains("개") || unit.contains("알") || unit.contains("장") || unit.contains("쪽")) {
            double grams = gramsPerPiece(ingredient.getIngredientName());
            warnings.add("개수 단위 재료는 평균 중량으로 추정했습니다: " + ingredient.getIngredientName());
            return new Factor(quantity * grams / 100.0, warnings);
        }

        warnings.add("알 수 없는 단위는 1회 제공량 기준으로 추정했습니다: " + ingredient.getIngredientName());
        return new Factor(Math.max(1.0, quantity), warnings);
    }

    private double gramsPerPiece(String ingredientName) {
        String normalized = normalizeName(ingredientName);
        if (normalized == null) {
            return 100.0;
        }
        if (normalized.contains("달걀") || normalized.contains("계란")) {
            return 50.0;
        }
        if (normalized.contains("바나나")) {
            return 100.0;
        }
        if (normalized.contains("사과")) {
            return 200.0;
        }
        if (normalized.contains("양파")) {
            return 150.0;
        }
        if (normalized.contains("감자") || normalized.contains("토마토")) {
            return 150.0;
        }
        if (normalized.contains("새우")) {
            return 20.0;
        }
        return 100.0;
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

    private Source resolveSource(boolean hasOriginal, boolean usedFoodId, boolean usedNameMatch, Map<String, Double> nutrients) {
        if (nutrients.isEmpty()) {
            return Source.MISSING;
        }
        if (usedFoodId && !usedNameMatch && !hasOriginal) {
            return Source.FOOD_ID_CALCULATED;
        }
        if (!usedFoodId && usedNameMatch && !hasOriginal) {
            return Source.NAME_ESTIMATED;
        }
        if (usedFoodId || usedNameMatch) {
            return Source.MIXED;
        }
        return Source.ORIGINAL;
    }

    private double confidence(Source source, Estimation estimation, Map<String, Double> nutrients,
                              BigDecimal calories, boolean usedEstimated) {
        if (source == Source.MISSING || nutrients.isEmpty() && calories == null) {
            return 0.0;
        }
        double base = switch (source) {
            case FOOD_ID_CALCULATED -> 0.9;
            case ORIGINAL -> 0.75;
            case MIXED -> 0.7;
            case NAME_ESTIMATED -> 0.55;
            case MISSING -> 0.0;
        };
        if (usedEstimated) {
            base = Math.min(base, 0.55 + estimation.matchRate() * 0.35);
        }
        if (!nutrients.containsKey("carbs")) {
            base -= 0.15;
        }
        if (!nutrients.containsKey("sugar")) {
            base -= 0.1;
        }
        return Math.max(0.0, Math.min(1.0, roundTwo(base)));
    }

    private double roundTwo(double value) {
        return Math.round(value * 100.0) / 100.0;
    }

    private enum Source {
        ORIGINAL,
        FOOD_ID_CALCULATED,
        NAME_ESTIMATED,
        MIXED,
        MISSING
    }

    public record Result(
            BigDecimal calories,
            String nutrientsJson,
            Map<String, Double> nutrients,
            String source,
            double confidence,
            List<String> warnings
    ) {
    }

    private record Estimation(
            Map<String, Double> nutrients,
            boolean usedFoodId,
            boolean usedNameMatch,
            double matchRate,
            List<String> warnings
    ) {
    }

    private record Factor(
            double multiplier,
            List<String> warnings
    ) {
    }
}
