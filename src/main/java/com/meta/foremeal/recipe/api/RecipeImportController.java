package com.meta.foremeal.recipe.api;

import com.meta.foremeal.recipe.service.RecipeImportService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/recipes/import")
public class RecipeImportController {

    private final RecipeImportService recipeImportService;

    public RecipeImportController(RecipeImportService recipeImportService) {
        this.recipeImportService = recipeImportService;
    }

    @PostMapping("/food-safety")
    public RecipeImportService.ImportResult importFoodSafetyRecipes(
            @RequestParam(defaultValue = "1") int start,
            @RequestParam(defaultValue = "20") int end
    ) {
        return recipeImportService.importFoodSafetyRecipes(start, end);
    }
}
