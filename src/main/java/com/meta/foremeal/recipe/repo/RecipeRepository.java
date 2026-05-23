package com.meta.foremeal.recipe.repo;

import com.meta.foremeal.recipe.domain.Recipe;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface RecipeRepository extends JpaRepository<Recipe, Long> {
    List<Recipe> findByCategory(String category);
    List<Recipe> findByDishType(String dishType);
    List<Recipe> findByDifficulty(String difficulty);
    List<Recipe> findByCookingTimeLessThanEqual(Integer cookingTime);
    boolean existsBySourceAndExternalId(String source, String externalId);

    @Query("select distinct r from Recipe r left join fetch r.ingredients")
    List<Recipe> findAllWithIngredients();

    @Query("select distinct r from Recipe r left join fetch r.ingredients where lower(r.title) like lower(concat('%', :title, '%'))")
    List<Recipe> findByTitleContainingWithIngredients(@Param("title") String title);
}
