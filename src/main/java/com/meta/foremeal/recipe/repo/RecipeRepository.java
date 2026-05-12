package com.meta.foremeal.recipe.repo;

import com.meta.foremeal.recipe.domain.Recipe;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RecipeRepository extends JpaRepository<Recipe, Long> {
    List<Recipe> findByCategory(String category);
    List<Recipe> findByDishType(String dishType);
    List<Recipe> findByDifficulty(String difficulty);
    List<Recipe> findByCookingTimeLessThanEqual(Integer cookingTime);
    boolean existsBySourceAndExternalId(String source, String externalId);
}
