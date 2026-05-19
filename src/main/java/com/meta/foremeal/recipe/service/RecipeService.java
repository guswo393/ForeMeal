package com.meta.foremeal.recipe.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.meta.foremeal.health.domain.Glucose;
import com.meta.foremeal.health.domain.HealthGoal;
import com.meta.foremeal.health.domain.HealthProfile;
import com.meta.foremeal.health.repo.GlucoseRepository;
import com.meta.foremeal.health.repo.HealthProfileRepository;
import com.meta.foremeal.meallog.domain.DailyIntakeSummary;
import com.meta.foremeal.meallog.repo.DailyIntakeSummaryRepository;
import com.meta.foremeal.pantry.domain.PantryItem;
import com.meta.foremeal.pantry.repository.PantryItemRepository;
import com.meta.foremeal.recipe.domain.Recipe;
import com.meta.foremeal.recipe.domain.RecipeIngredient;
import com.meta.foremeal.recipe.domain.RecipeStep;
import com.meta.foremeal.recipe.domain.Substitute;
import com.meta.foremeal.recipe.dto.RecipeDto;
import com.meta.foremeal.recipe.exception.RecipeNotFoundException;
import com.meta.foremeal.recipe.repo.RecipeRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Set;

@Service
public class RecipeService {

    private final RecipeRepository recipeRepository;
    private final PantryItemRepository pantryItemRepository;
    private final HealthProfileRepository healthProfileRepository;
    private final DailyIntakeSummaryRepository summaryRepository;
    private final GlucoseRepository glucoseRepository;
    private final ObjectMapper objectMapper;

    public RecipeService(RecipeRepository recipeRepository,
                         PantryItemRepository pantryItemRepository,
                         HealthProfileRepository healthProfileRepository,
                         DailyIntakeSummaryRepository summaryRepository,
                         GlucoseRepository glucoseRepository,
                         ObjectMapper objectMapper) {
        this.recipeRepository = recipeRepository;
        this.pantryItemRepository = pantryItemRepository;
        this.healthProfileRepository = healthProfileRepository;
        this.summaryRepository = summaryRepository;
        this.glucoseRepository = glucoseRepository;
        this.objectMapper = objectMapper;
    }

    @Transactional
    public RecipeDto.Response create(RecipeDto.CreateRequest req) {
        Recipe recipe = new Recipe(
                req.title(),
                req.description(),
                req.category(),
                req.dishType(),
                req.difficulty(),
                req.cookingTime(),
                req.servings(),
                req.totalCalories(),
                req.totalNutrients(),
                req.giLevel(),
                req.imageUri()
        );

        toIngredients(req.ingredients()).forEach(recipe::addIngredient);
        toSteps(req.steps()).forEach(recipe::addStep);

        return toResponse(recipeRepository.save(recipe));
    }

    @Transactional(readOnly = true)
    public List<RecipeDto.Response> getAll(String category, String dishType, String difficulty, Integer maxCookingTime) {
        return recipeRepository.findAll()
                .stream()
                .filter(recipe -> matches(category, recipe.getCategory()))
                .filter(recipe -> matches(dishType, recipe.getDishType()))
                .filter(recipe -> matches(difficulty, recipe.getDifficulty()))
                .filter(recipe -> maxCookingTime == null
                        || recipe.getCookingTime() != null && recipe.getCookingTime() <= maxCookingTime)
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public RecipeDto.Response getById(Long recipeId) {
        return toResponse(findRecipe(recipeId));
    }

    @Transactional(readOnly = true)
    public List<RecipeDto.RecommendationResponse> recommendByPantry(Long userId, int limit) {
        if (userId == null) {
            throw new IllegalArgumentException("userId is required.");
        }

        HealthContext healthContext = loadHealthContext(userId);
        List<PantryItem> pantryItems = pantryItemRepository.findAllByUserIdWithFoodMaster(userId);
        Set<Long> pantryFoodIds = new HashSet<>();
        Set<String> pantryNames = new HashSet<>();

        for (PantryItem pantryItem : pantryItems) {
            if (pantryItem.getFoodMaster() != null && pantryItem.getFoodMaster().getFoodId() != null) {
                pantryFoodIds.add(pantryItem.getFoodMaster().getFoodId());
            }
            addNormalizedName(pantryNames, pantryItem.getDisplayName());
            addNormalizedName(pantryNames, pantryItem.getCustomName());
            if (pantryItem.getFoodMaster() != null) {
                addNormalizedName(pantryNames, pantryItem.getFoodMaster().getFoodName());
            }
        }

        int normalizedLimit = limit <= 0 ? 10 : Math.min(limit, 50);

        return recipeRepository.findAllWithIngredients().stream()
                .map(recipe -> toRecommendation(recipe, pantryFoodIds, pantryNames, healthContext))
                .filter(response -> response.matchedIngredientCount() > 0)
                .sorted(Comparator
                        .comparingInt(RecipeDto.RecommendationResponse::matchedIngredientCount).reversed()
                        .thenComparing(Comparator.comparingDouble(RecipeDto.RecommendationResponse::matchRate).reversed())
                        .thenComparing(Comparator.comparingDouble(RecipeDto.RecommendationResponse::healthScore).reversed())
                        .thenComparingInt(RecipeDto.RecommendationResponse::missingIngredientCount)
                        .thenComparing(response -> response.cookingTime() == null ? Integer.MAX_VALUE : response.cookingTime())
                        .thenComparing(RecipeDto.RecommendationResponse::title))
                .limit(normalizedLimit)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<RecipeDto.RecommendationResponse> recommendByHealth(Long userId, int limit) {
        if (userId == null) {
            throw new IllegalArgumentException("userId is required.");
        }

        HealthContext healthContext = loadHealthContext(userId);
        int normalizedLimit = limit <= 0 ? 10 : Math.min(limit, 50);

        return recipeRepository.findAllWithIngredients().stream()
                .map(recipe -> toRecommendation(recipe, Set.of(), Set.of(), healthContext))
                .sorted(Comparator
                        .comparingDouble(RecipeDto.RecommendationResponse::healthScore).reversed()
                        .thenComparing(response -> response.cookingTime() == null ? Integer.MAX_VALUE : response.cookingTime())
                        .thenComparing(RecipeDto.RecommendationResponse::title))
                .limit(normalizedLimit)
                .toList();
    }

    @Transactional
    public RecipeDto.Response update(Long recipeId, RecipeDto.UpdateRequest req) {
        Recipe recipe = findRecipe(recipeId);
        recipe.update(
                req.title(),
                req.description(),
                req.category(),
                req.dishType(),
                req.difficulty(),
                req.cookingTime(),
                req.servings(),
                req.totalCalories(),
                req.totalNutrients(),
                req.giLevel(),
                req.imageUri()
        );

        recipe.replaceIngredients(toIngredients(req.ingredients()));
        recipe.replaceSteps(toSteps(req.steps()));

        return toResponse(recipe);
    }

    @Transactional
    public void delete(Long recipeId) {
        Recipe recipe = findRecipe(recipeId);
        recipeRepository.delete(recipe);
    }

    private Recipe findRecipe(Long recipeId) {
        return recipeRepository.findById(recipeId)
                .orElseThrow(RecipeNotFoundException::new);
    }

    private List<RecipeIngredient> toIngredients(List<RecipeDto.IngredientRequest> requests) {
        if (requests == null) {
            return List.of();
        }

        return requests.stream()
                .map(it -> {
                    RecipeIngredient ingredient = new RecipeIngredient(
                        it.foodId(),
                        it.ingredientName(),
                        it.quantity(),
                        it.unit()
                    );

                    toSubstitutes(it.substitutes()).forEach(ingredient::addSubstitute);
                    return ingredient;
                })
                .toList();
    }

    private List<Substitute> toSubstitutes(List<RecipeDto.SubstituteRequest> requests) {
        if (requests == null) {
            return List.of();
        }

        return requests.stream()
                .map(it -> new Substitute(
                        it.conversionRatio(),
                        it.description()
                ))
                .toList();
    }

    private List<RecipeStep> toSteps(List<RecipeDto.StepRequest> requests) {
        if (requests == null) {
            return List.of();
        }

        return requests.stream()
                .map(it -> new RecipeStep(
                        it.stepOrder(),
                        it.instruction(),
                        it.imageUri()
                ))
                .toList();
    }

    private RecipeDto.Response toResponse(Recipe recipe) {
        List<RecipeDto.IngredientResponse> ingredients = recipe.getIngredients().stream()
                .map(it -> new RecipeDto.IngredientResponse(
                        it.getItemId(),
                        it.getFoodId(),
                        it.getIngredientName(),
                        it.getQuantity(),
                        it.getUnit(),
                        toSubstituteResponses(it)
                ))
                .toList();

        List<RecipeDto.StepResponse> steps = recipe.getSteps().stream()
                .sorted(Comparator.comparing(RecipeStep::getStepOrder))
                .map(it -> new RecipeDto.StepResponse(
                        it.getStepId(),
                        it.getStepOrder(),
                        it.getInstruction(),
                        it.getImageUri()
                ))
                .toList();

        return new RecipeDto.Response(
                recipe.getRecipeId(),
                recipe.getTitle(),
                recipe.getDescription(),
                recipe.getCategory(),
                recipe.getDishType(),
                recipe.getDifficulty(),
                recipe.getCookingTime(),
                recipe.getServings(),
                recipe.getTotalCalories(),
                recipe.getTotalNutrients(),
                recipe.getGiLevel(),
                recipe.getImageUri(),
                ingredients,
                steps
        );
    }

    private List<RecipeDto.SubstituteResponse> toSubstituteResponses(RecipeIngredient ingredient) {
        return ingredient.getSubstitutes().stream()
                .map(it -> new RecipeDto.SubstituteResponse(
                        it.getSubId(),
                        it.getConversionRatio(),
                        it.getDescription()
                ))
                .toList();
    }

    private RecipeDto.RecommendationResponse toRecommendation(
            Recipe recipe,
            Set<Long> pantryFoodIds,
            Set<String> pantryNames,
            HealthContext healthContext
    ) {
        List<String> matchedIngredients = new ArrayList<>();
        List<String> missingIngredients = new ArrayList<>();

        for (RecipeIngredient ingredient : recipe.getIngredients()) {
            if (matchesPantry(ingredient, pantryFoodIds, pantryNames)) {
                matchedIngredients.add(ingredient.getIngredientName());
            } else {
                missingIngredients.add(ingredient.getIngredientName());
            }
        }

        int totalIngredients = matchedIngredients.size() + missingIngredients.size();
        double matchRate = totalIngredients == 0 ? 0.0 : (double) matchedIngredients.size() / totalIngredients;
        HealthScore healthScore = calculateHealthScore(recipe, healthContext);

        return new RecipeDto.RecommendationResponse(
                recipe.getRecipeId(),
                recipe.getTitle(),
                recipe.getDescription(),
                recipe.getCategory(),
                recipe.getDishType(),
                recipe.getDifficulty(),
                recipe.getCookingTime(),
                recipe.getServings(),
                recipe.getGiLevel(),
                recipe.getImageUri(),
                healthScore.score(),
                matchedIngredients.size(),
                missingIngredients.size(),
                Math.round(matchRate * 100.0) / 100.0,
                matchedIngredients,
                missingIngredients,
                healthScore.reasons()
        );
    }

    private boolean matchesPantry(RecipeIngredient ingredient, Set<Long> pantryFoodIds, Set<String> pantryNames) {
        if (ingredient.getFoodId() != null && pantryFoodIds.contains(ingredient.getFoodId())) {
            return true;
        }

        String ingredientName = normalizeName(ingredient.getIngredientName());
        if (ingredientName == null) {
            return false;
        }

        return pantryNames.stream()
                .anyMatch(pantryName -> pantryName.contains(ingredientName) || ingredientName.contains(pantryName));
    }

    private HealthContext loadHealthContext(Long userId) {
        HealthProfile profile = healthProfileRepository.findByUserId(userId).orElse(null);
        LocalDate today = LocalDate.now();
        DailyIntakeSummary summary = summaryRepository.findByUserIdAndSummaryDate(userId, today)
                .orElseGet(() -> new DailyIntakeSummary(userId, today));
        List<Glucose> glucoseRecords = glucoseRepository.findByUserIdAndMeasuredAtBetweenOrderByMeasuredAtAsc(
                userId,
                today.atStartOfDay(),
                today.plusDays(1).atStartOfDay()
        );

        double latestGlucose = glucoseRecords.isEmpty()
                ? 0.0
                : glucoseRecords.get(glucoseRecords.size() - 1).getGlucoseValue().doubleValue();
        double targetCalories = profile != null && profile.getGoal() == HealthGoal.WEIGHT_GAIN ? 2400.0
                : profile != null && profile.getGoal() == HealthGoal.WEIGHT_LOSS ? 1600.0
                : 2000.0;

        return new HealthContext(
                profile,
                summary.getTotalCalories().doubleValue(),
                summary.getTotalSodium().doubleValue(),
                summary.getTotalSugar().doubleValue(),
                summary.getTotalCarbs().doubleValue(),
                latestGlucose,
                targetCalories
        );
    }

    private HealthScore calculateHealthScore(Recipe recipe, HealthContext context) {
        double score = 70.0;
        List<String> reasons = new ArrayList<>();

        double calories = recipe.getTotalCalories() == null ? 0.0 : recipe.getTotalCalories().doubleValue();
        double carbs = nutrient(recipe, "carbs");
        double sugar = nutrient(recipe, "sugar");
        double sodium = nutrient(recipe, "sodium");
        String giLevel = recipe.getGiLevel() == null ? "" : recipe.getGiLevel().toUpperCase(Locale.ROOT);
        double remainingCalories = context.targetCalories() - context.todayCalories();

        if (remainingCalories > 0 && calories > 0 && calories <= remainingCalories) {
            score += 8;
            reasons.add("오늘 남은 칼로리 범위에 맞아요.");
        } else if (remainingCalories > 0 && calories > remainingCalories) {
            score -= 15;
            reasons.add("오늘 남은 칼로리보다 열량이 높아요.");
        }

        boolean glucoseSensitive = context.hasDiabetes() || context.latestGlucose() >= 180.0;
        if (glucoseSensitive) {
            if ("LOW".equals(giLevel)) {
                score += 14;
                reasons.add("혈당 관리에 유리한 LOW GI 레시피예요.");
            } else if ("MEDIUM".equals(giLevel)) {
                score -= 6;
                reasons.add("GI가 중간이라 양 조절이 좋아요.");
            } else if ("HIGH".equals(giLevel)) {
                score -= 25;
                reasons.add("혈당 관리 중에는 HIGH GI 레시피를 주의하세요.");
            }

            if (carbs > 60.0) {
                score -= 15;
                reasons.add("탄수화물이 높은 편이에요.");
            }
            if (sugar > 15.0) {
                score -= 15;
                reasons.add("당류가 높은 편이에요.");
            }
        }

        boolean sodiumSensitive = context.hasHypertension()
                || context.goal() == HealthGoal.LOW_SODIUM
                || context.todaySodium() > 1500.0;
        if (sodiumSensitive && sodium > 800.0) {
            score -= sodium > 1200.0 ? 25 : 12;
            reasons.add("나트륨 섭취를 줄이는 날에는 주의가 필요해요.");
        }

        if (context.goal() == HealthGoal.WEIGHT_LOSS) {
            if (calories > 600.0) {
                score -= 15;
                reasons.add("감량 목표에는 열량이 높은 편이에요.");
            } else if (calories > 0 && calories <= 400.0) {
                score += 8;
                reasons.add("감량 목표에 맞는 가벼운 레시피예요.");
            }
        }

        if (context.avoidIngredients() != null) {
            for (RecipeIngredient ingredient : recipe.getIngredients()) {
                String ingredientName = normalizeName(ingredient.getIngredientName());
                if (ingredientName != null && context.avoidIngredients().contains(ingredientName)) {
                    score -= 40;
                    reasons.add("피해야 할 재료가 포함되어 있어요: " + ingredient.getIngredientName());
                }
            }
        }

        if (reasons.isEmpty()) {
            reasons.add("건강 정보 기준에서 큰 제한 없이 먹기 좋은 레시피예요.");
        }

        return new HealthScore(Math.max(0.0, Math.min(100.0, Math.round(score * 10.0) / 10.0)), reasons);
    }

    private double nutrient(Recipe recipe, String key) {
        if (!StringUtils.hasText(recipe.getTotalNutrients())) {
            return 0.0;
        }

        try {
            JsonNode node = objectMapper.readTree(recipe.getTotalNutrients());
            JsonNode value = node.get(key);
            return value == null || !value.isNumber() ? 0.0 : value.doubleValue();
        } catch (Exception e) {
            return 0.0;
        }
    }

    private void addNormalizedName(Set<String> names, String value) {
        String normalized = normalizeName(value);
        if (normalized != null) {
            names.add(normalized);
        }
    }

    private String normalizeName(String value) {
        if (!StringUtils.hasText(value)) {
            return null;
        }
        return value.replaceAll("\\s+", "").toLowerCase(Locale.ROOT);
    }

    private boolean matches(String expected, String actual) {
        return expected == null || expected.isBlank() || expected.equals(actual);
    }

    private record HealthContext(
            HealthProfile profile,
            double todayCalories,
            double todaySodium,
            double todaySugar,
            double todayCarbs,
            double latestGlucose,
            double targetCalories
    ) {
        boolean hasDiabetes() {
            return profile != null && profile.isHasDiabetes();
        }

        boolean hasHypertension() {
            return profile != null && profile.isHasHypertension();
        }

        HealthGoal goal() {
            return profile == null ? null : profile.getGoal();
        }

        String avoidIngredients() {
            if (profile == null || profile.getAvoidIngredients() == null) {
                return null;
            }
            return profile.getAvoidIngredients().replaceAll("\\s+", "").toLowerCase(Locale.ROOT);
        }
    }

    private record HealthScore(double score, List<String> reasons) {
    }
}
