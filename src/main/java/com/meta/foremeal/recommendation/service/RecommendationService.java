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

    private static final BigDecimal HIGH_DAILY_CALORIES = new BigDecimal("1800");
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
            appendReason(reason, "LOW GI 레시피");
        }

        if (isLowCalorie(recipe)) {
            score += 20;
            appendReason(reason, "가벼운 칼로리");
        }

        if (isQuick(recipe)) {
            score += 10;
            appendReason(reason, "짧은 조리시간");
        }

        if (isEasy(recipe)) {
            score += 10;
            appendReason(reason, "쉬운 난이도");
        }

        if (isHigh(summary.getTotalCalories(), HIGH_DAILY_CALORIES) && isLowCalorie(recipe)) {
            score += 20;
            appendReason(reason, "오늘 섭취 칼로리 기준 적합");
        }

        if ((isHigh(summary.getTotalSugar(), HIGH_DAILY_SUGAR) || isHigh(summary.getTotalCarbs(), HIGH_DAILY_CARBS))
                && isLowGi(recipe)) {
            score += 25;
            appendReason(reason, "오늘 당/탄수화물 섭취 기준 적합");
        }

        if (recentlyEatenRecipeIds.contains(recipe.getRecipeId())) {
            score -= 40;
            appendReason(reason, "최근 먹은 레시피라 감점");
        }

        if (reason.isEmpty()) {
            appendReason(reason, "기본 추천 레시피");
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
