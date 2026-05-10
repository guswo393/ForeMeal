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
    public List<RecipeDto.Response> getAll() {
        return recipeService.getAll();
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
