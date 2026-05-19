package com.meta.foremeal.recipe.api;

import com.meta.foremeal.recipe.dto.RecipeDto;
import com.meta.foremeal.recipe.service.RecipeService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/recipes")
public class RecipeController {

    private final RecipeService recipeService;

    public RecipeController(RecipeService recipeService) {
        this.recipeService = recipeService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public RecipeDto.Response create(@RequestBody @Valid RecipeDto.CreateRequest request) {
        return recipeService.create(request);
    }

    @GetMapping
    public List<RecipeDto.Response> getAll(
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String dishType,
            @RequestParam(required = false) String difficulty,
            @RequestParam(required = false) Integer maxCookingTime
    ) {
        return recipeService.getAll(category, dishType, difficulty, maxCookingTime);
    }

    @GetMapping("/recommendations")
    public List<RecipeDto.RecommendationResponse> recommend(
            @RequestParam Long userId,
            @RequestParam(defaultValue = "10") int limit
    ) {
        return recipeService.recommendByPantry(userId, limit);
    }

    @GetMapping("/recommendations/pantry")
    public List<RecipeDto.RecommendationResponse> recommendByPantry(
            @RequestParam Long userId,
            @RequestParam(defaultValue = "10") int limit
    ) {
        return recipeService.recommendByPantry(userId, limit);
    }

    @GetMapping("/recommendations/health")
    public List<RecipeDto.RecommendationResponse> recommendByHealth(
            @RequestParam Long userId,
            @RequestParam(defaultValue = "10") int limit
    ) {
        return recipeService.recommendByHealth(userId, limit);
    }

    @GetMapping("/{recipeId}")
    public RecipeDto.Response getById(@PathVariable Long recipeId) {
        return recipeService.getById(recipeId);
    }

    @PutMapping("/{recipeId}")
    public RecipeDto.Response update(@PathVariable Long recipeId,
                                     @RequestBody @Valid RecipeDto.UpdateRequest request) {
        return recipeService.update(recipeId, request);
    }

    @DeleteMapping("/{recipeId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long recipeId) {
        recipeService.delete(recipeId);
    }
}
