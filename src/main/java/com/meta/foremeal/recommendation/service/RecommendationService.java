package com.meta.foremeal.recommendation.service;

import com.meta.foremeal.meallog.domain.DailyIntakeSummary;
import com.meta.foremeal.meallog.domain.MealLog;
import com.meta.foremeal.meallog.repo.DailyIntakeSummaryRepository;
import com.meta.foremeal.meallog.repo.MealLogRepository;
import com.meta.foremeal.recipe.domain.Recipe;
import com.meta.foremeal.recipe.repo.RecipeRepository;
import com.meta.foremeal.recommendation.dto.RecommendationDto;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
public class RecommendationService {

    private static final BigDecimal DAILY_CALORIE_GOAL = new BigDecimal("2200");
    private static final BigDecimal CALORIE_LIGHT_MEAL_RATIO = new BigDecimal("0.8");
    private static final BigDecimal HIGH_DAILY_SUGAR = new BigDecimal("50");
    private static final BigDecimal HIGH_DAILY_CARBS = new BigDecimal("250");
    private static final BigDecimal LOW_RECIPE_CALORIES = new BigDecimal("500");

    private final RecipeRepository recipeRepository;
    private final MealLogRepository mealLogRepository;
    private final DailyIntakeSummaryRepository summaryRepository;

    public RecommendationService(RecipeRepository recipeRepository,
                                 MealLogRepository mealLogRepository,
                                 DailyIntakeSummaryRepository summaryRepository) {
        this.recipeRepository = recipeRepository;
        this.mealLogRepository = mealLogRepository;
        this.summaryRepository = summaryRepository;
    }

    @Transactional(readOnly = true)
    public List<RecommendationDto.RecipeRecommendationResponse> recommendRecipes(Long userId, LocalDate date, int limit) {
        LocalDate targetDate = date == null ? LocalDate.now() : date;
        DailyIntakeSummary summary = summaryRepository
                .findByUserIdAndSummaryDate(userId, targetDate)
                .orElseGet(() -> new DailyIntakeSummary(userId, targetDate));

        Set<Long> recentlyEatenRecipeIds = getRecentlyEatenRecipeIds(userId, targetDate);
        int safeLimit = Math.max(1, Math.min(limit, 20));

        return recipeRepository.findAll()
                .stream()
                .map(recipe -> score(recipe, summary, recentlyEatenRecipeIds))
                .sorted(Comparator
                        .comparingInt(ScoredRecipe::score).reversed()
                        .thenComparing(scored -> scored.recipe().getRecipeId()))
                .limit(safeLimit)
                .map(this::toResponse)
                .toList();
    }

    private Set<Long> getRecentlyEatenRecipeIds(Long userId, LocalDate date) {
        LocalDateTime from = date.minusDays(7).atStartOfDay();
        LocalDateTime to = date.plusDays(1).atStartOfDay();

        List<MealLog> mealLogs = mealLogRepository.findByUserIdAndEatenAtBetweenOrderByEatenAtAsc(userId, from, to);
        Set<Long> recipeIds = new HashSet<>();

        for (MealLog mealLog : mealLogs) {
            if (mealLog.getRecipeId() != null) {
                recipeIds.add(mealLog.getRecipeId());
            }
        }

        return recipeIds;
    }

    private ScoredRecipe score(Recipe recipe, DailyIntakeSummary summary, Set<Long> recentlyEatenRecipeIds) {
        int score = 0;
        StringBuilder reason = new StringBuilder();

        if (isLowGi(recipe)) {
            score += 25;
            appendReason(reason, "혈당 부담이 낮은 LOW GI 레시피예요.");
        }

        if (isLowCalorie(recipe)) {
            score += 20;
            appendReason(reason, "총 칼로리가 낮아 가볍게 먹기 좋아요.");
        }

        if (isQuick(recipe)) {
            score += 10;
            appendReason(reason, "30분 이내로 조리할 수 있어요.");
        }

        if (isEasy(recipe)) {
            score += 10;
            appendReason(reason, "난이도가 쉬워 부담 없이 만들 수 있어요.");
        }

        if (needsLightMeal(summary) && isLowCalorie(recipe)) {
            score += 20;
            appendReason(reason, "오늘 권장 섭취량에 가까워져 가벼운 레시피를 우선 추천했어요.");
        }

        if ((isHigh(summary.getTotalSugar(), HIGH_DAILY_SUGAR) || isHigh(summary.getTotalCarbs(), HIGH_DAILY_CARBS))
                && isLowGi(recipe)) {
            score += 25;
            appendReason(reason, "오늘 당/탄수화물 섭취가 높아 LOW GI 레시피를 우선 추천했어요.");
        }

        if (recentlyEatenRecipeIds.contains(recipe.getRecipeId())) {
            score -= 40;
            appendReason(reason, "최근에 먹은 레시피라 추천 우선순위를 낮췄어요.");
        }

        if (reason.isEmpty()) {
            appendReason(reason, "현재 식사 기록을 기준으로 무난하게 추천할 수 있는 레시피예요.");
        }

        return new ScoredRecipe(recipe, score, reason.toString());
    }

    private boolean isLowGi(Recipe recipe) {
        return recipe.getGiLevel() != null && recipe.getGiLevel().equalsIgnoreCase("LOW");
    }

    private boolean isLowCalorie(Recipe recipe) {
        return recipe.getTotalCalories() != null
                && recipe.getTotalCalories().compareTo(LOW_RECIPE_CALORIES) <= 0;
    }

    private boolean isQuick(Recipe recipe) {
        return recipe.getCookingTime() != null && recipe.getCookingTime() <= 30;
    }

    private boolean isEasy(Recipe recipe) {
        return recipe.getDifficulty() != null && recipe.getDifficulty().equalsIgnoreCase("EASY");
    }

    private boolean isHigh(BigDecimal value, BigDecimal threshold) {
        return value != null && value.compareTo(threshold) >= 0;
    }

    private boolean needsLightMeal(DailyIntakeSummary summary) {
        BigDecimal threshold = DAILY_CALORIE_GOAL.multiply(CALORIE_LIGHT_MEAL_RATIO);
        return summary.getTotalCalories() != null
                && summary.getTotalCalories().compareTo(threshold) >= 0;
    }

    private void appendReason(StringBuilder reason, String message) {
        if (!reason.isEmpty()) {
            reason.append(", ");
        }
        reason.append(message);
    }

    private RecommendationDto.RecipeRecommendationResponse toResponse(ScoredRecipe scoredRecipe) {
        Recipe recipe = scoredRecipe.recipe();

        return new RecommendationDto.RecipeRecommendationResponse(
                recipe.getRecipeId(),
                recipe.getTitle(),
                recipe.getDescription(),
                recipe.getCategory(),
                recipe.getDishType(),
                recipe.getDifficulty(),
                recipe.getCookingTime(),
                recipe.getServings(),
                recipe.getTotalCalories(),
                recipe.getGiLevel(),
                scoredRecipe.score(),
                scoredRecipe.reason()
        );
    }

    private record ScoredRecipe(
            Recipe recipe,
            int score,
            String reason
    ) {}
}
