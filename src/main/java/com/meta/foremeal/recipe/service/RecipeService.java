package com.meta.foremeal.recipe.service;

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
import java.util.Set;

@Service
public class RecipeService {

    private final RecipeRepository recipeRepository;
    private final PantryItemRepository pantryItemRepository;

    public RecipeService(RecipeRepository recipeRepository, PantryItemRepository pantryItemRepository) {
        this.recipeRepository = recipeRepository;
        this.pantryItemRepository = pantryItemRepository;
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
                .map(recipe -> toRecommendation(recipe, pantryFoodIds, pantryNames))
                .filter(response -> response.matchedIngredientCount() > 0)
                .sorted(Comparator
                        .comparingInt(RecipeDto.RecommendationResponse::matchedIngredientCount).reversed()
                        .thenComparing(Comparator.comparingDouble(RecipeDto.RecommendationResponse::matchRate).reversed())
                        .thenComparingInt(RecipeDto.RecommendationResponse::missingIngredientCount)
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
            Set<String> pantryNames
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
                matchedIngredients.size(),
                missingIngredients.size(),
                Math.round(matchRate * 100.0) / 100.0,
                matchedIngredients,
                missingIngredients
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
}
