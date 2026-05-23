package com.meta.foremeal.meallog.service;

import com.meta.foremeal.foodmaster.domain.FoodMasterEntity;
import com.meta.foremeal.foodmaster.repo.FoodMasterRepository;
import com.meta.foremeal.meallog.api.dto.PredictionRequest;
import com.meta.foremeal.meallog.api.dto.PredictionFoodSearchResponse;
import com.meta.foremeal.meallog.api.dto.PredictionResponse;
import com.meta.foremeal.meallog.domain.DailyVitalSummary;
import com.meta.foremeal.meallog.domain.GlucosePrediction;
import com.meta.foremeal.meallog.repo.DailyVitalSummaryRepository;
import com.meta.foremeal.meallog.repo.GlucosePredictionRepository;
import com.meta.foremeal.recipe.domain.Recipe;
import com.meta.foremeal.recipe.repo.RecipeRepository;
import com.meta.foremeal.recipe.service.RecipeNutritionCalculator;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class GlucosePredictionService {

    private final RestTemplate restTemplate;
    private final GlucosePredictionRepository glucosePredictionRepository;
    private final DailyVitalSummaryRepository dailyVitalSummaryRepository;
    private final FoodMasterRepository foodMasterRepository;
    private final RecipeRepository recipeRepository;
    private final RecipeNutritionCalculator recipeNutritionCalculator;

    @Value("${foremeal.ai-server-url:http://localhost:8000}")
    private String aiServerUrl;

    public GlucosePredictionService(RestTemplate restTemplate,
                                    GlucosePredictionRepository glucosePredictionRepository,
                                    DailyVitalSummaryRepository dailyVitalSummaryRepository,
                                    FoodMasterRepository foodMasterRepository,
                                    RecipeRepository recipeRepository,
                                    RecipeNutritionCalculator recipeNutritionCalculator) {
        this.restTemplate = restTemplate;
        this.glucosePredictionRepository = glucosePredictionRepository;
        this.dailyVitalSummaryRepository = dailyVitalSummaryRepository;
        this.foodMasterRepository = foodMasterRepository;
        this.recipeRepository = recipeRepository;
        this.recipeNutritionCalculator = recipeNutritionCalculator;
    }

    @Transactional
    public PredictionResponse predictAndSave(PredictionRequest request) {
        PredictionResponse response = restTemplate.postForObject(
                aiServerUrl + "/predict",
                request,
                PredictionResponse.class
        );

        if (response == null) {
            throw new IllegalStateException("Python prediction server returned empty response.");
        }

        Long foodId = resolveFoodId(request);
        Integer pred1hMgdl = resolvePred1hMgdl(response.getPredictionCurve());

        GlucosePrediction savedPrediction = glucosePredictionRepository.save(
                new GlucosePrediction(
                        request.getMealId(),
                        request.getUserId(),
                        foodId,
                        "python-rule-v1",
                        pred1hMgdl,
                        response.getRiskLevel()
                )
        );

        Map<String, Object> graphData = Map.of(
                "curve", toGraphPoints(response.getPredictionCurve()),
                "riskLevel", response.getRiskLevel(),
                "predictedPeak", response.getPredictedPeak()
        );

        DailyVitalSummary savedSummary = dailyVitalSummaryRepository.save(
                new DailyVitalSummary(
                        BigDecimal.valueOf(response.getPredictedPeak()),
                        graphData,
                        savedPrediction.getPredictionId(),
                        request.getMealId(),
                        request.getUserId(),
                        foodId
                )
        );

        response.setPredictionId(savedPrediction.getPredictionId());
        response.setDailyVitalSummaryId(savedSummary.getPredId());

        return response;
    }

    public List<DailyVitalSummary> getGraphData(Long userId) {
        return dailyVitalSummaryRepository.findByUserIdOrderByPredIdDesc(userId);
    }

    @Transactional(readOnly = true)
    public List<PredictionFoodSearchResponse> searchPredictionFoods(String query) {
        if (!StringUtils.hasText(query)) {
            return List.of();
        }

        String keyword = query.trim();
        Map<String, PredictionFoodSearchResponse> results = new LinkedHashMap<>();

        for (FoodMasterEntity food : searchFoods(keyword)) {
            results.put("FOOD-" + food.getFoodId(), new PredictionFoodSearchResponse(
                    "FOOD",
                    food.getFoodId(),
                    food.getFoodId(),
                    null,
                    food.getFoodName(),
                    food.getCalories(),
                    food.getCarbs(),
                    food.getSugar(),
                    food.getSodium(),
                    "FOOD_MASTER",
                    1.0,
                    List.of()
            ));
        }

        for (Recipe recipe : searchRecipes(keyword)) {
            RecipeNutritionCalculator.Result nutrition = recipeNutritionCalculator.calculate(recipe);
            results.put("RECIPE-" + recipe.getRecipeId(), new PredictionFoodSearchResponse(
                    "RECIPE",
                    recipe.getRecipeId(),
                    null,
                    recipe.getRecipeId(),
                    recipe.getTitle(),
                    nutrition.calories() == null ? null : nutrition.calories().doubleValue(),
                    nutrition.nutrients().get("carbs"),
                    nutrition.nutrients().get("sugar"),
                    nutrition.nutrients().get("sodium"),
                    nutrition.source(),
                    nutrition.confidence(),
                    nutrition.warnings()
            ));
        }

        return results.values().stream().limit(20).toList();
    }

    private List<FoodMasterEntity> searchFoods(String keyword) {
        Map<Long, FoodMasterEntity> foods = new LinkedHashMap<>();
        foodMasterRepository.findByFoodNameContainingIgnoreCase(keyword)
                .forEach(food -> foods.put(food.getFoodId(), food));

        if (foods.isEmpty()) {
            for (String token : keyword.split("\\s+")) {
                if (token.length() < 2) {
                    continue;
                }
                foodMasterRepository.findByFoodNameContainingIgnoreCase(token)
                        .forEach(food -> foods.put(food.getFoodId(), food));
            }
        }

        return new ArrayList<>(foods.values());
    }

    private List<Recipe> searchRecipes(String keyword) {
        Map<Long, Recipe> recipes = new LinkedHashMap<>();
        recipeRepository.findByTitleContainingWithIngredients(keyword)
                .forEach(recipe -> recipes.put(recipe.getRecipeId(), recipe));

        if (recipes.isEmpty()) {
            for (String token : keyword.split("\\s+")) {
                if (token.length() < 2) {
                    continue;
                }
                recipeRepository.findByTitleContainingWithIngredients(token)
                        .forEach(recipe -> recipes.put(recipe.getRecipeId(), recipe));
            }
        }

        return new ArrayList<>(recipes.values());
    }

    private Long resolveFoodId(PredictionRequest request) {
        if (request.getFoodId() != null) {
            return request.getFoodId();
        }
        if (request.getFoods() != null && !request.getFoods().isEmpty()) {
            return request.getFoods().get(0).getFoodId();
        }
        return null;
    }

    private Integer resolvePred1hMgdl(List<Map<String, Object>> curve) {
        if (curve == null || curve.isEmpty()) {
            return null;
        }
        int index = Math.min(2, curve.size() - 1);
        Object value = curve.get(index).get("glucoseMgdl");
        if (value instanceof Number number) {
            return (int) Math.round(number.doubleValue());
        }
        return null;
    }

    private List<Map<String, Object>> toGraphPoints(List<Map<String, Object>> curve) {
        if (curve == null) {
            return List.of();
        }
        return curve;
    }
}
