package com.meta.foremeal.recipe.service;

import com.meta.foremeal.recipe.domain.Recipe;
import com.meta.foremeal.recipe.domain.RecipeIngredient;
import com.meta.foremeal.recipe.domain.RecipeStep;
import com.meta.foremeal.recipe.dto.RecipeDto;
import com.meta.foremeal.recipe.exception.RecipeNotFoundException;
import com.meta.foremeal.recipe.repo.RecipeRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;

@Service
public class RecipeService {

    private final RecipeRepository recipeRepository;

    public RecipeService(RecipeRepository recipeRepository) {
        this.recipeRepository = recipeRepository;
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
                req.giLevel()
        );

        toIngredients(req.ingredients()).forEach(recipe::addIngredient);
        toSteps(req.steps()).forEach(recipe::addStep);

        return toResponse(recipeRepository.save(recipe));
    }

    @Transactional(readOnly = true)
    public List<RecipeDto.Response> getAll() {
        return recipeRepository.findAll()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public RecipeDto.Response getById(Long recipeId) {
        return toResponse(findRecipe(recipeId));
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
                req.giLevel()
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
                .map(it -> new RecipeIngredient(
                        it.foodId(),
                        it.ingredientName(),
                        it.quantity(),
                        it.unit()
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
                        it.getUnit()
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
                ingredients,
                steps
        );
    }
}
