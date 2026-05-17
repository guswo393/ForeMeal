package com.meta.foremeal.recipe.domain;

import jakarta.persistence.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "recipe")
public class Recipe {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "recipe_id")
    private Long recipeId;

    @Column(name = "title", nullable = false, length = 255)
    private String title;

    @Column(name = "external_id", length = 100)
    private String externalId;

    @Column(name = "source", length = 50)
    private String source;

    @Column(name = "description", columnDefinition = "text")
    private String description;

    @Column(name = "category", length = 100)
    private String category;

    @Column(name = "dish_type", length = 100)
    private String dishType;

    @Column(name = "difficulty", length = 50)
    private String difficulty;

    @Column(name = "cooking_time")
    private Integer cookingTime;

    @Column(name = "servings")
    private Integer servings;

    @Column(name = "total_calories", precision = 10, scale = 2)
    private BigDecimal totalCalories;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "total_nutrients", columnDefinition = "jsonb")
    private String totalNutrients;

    @Column(name = "gi_level", length = 50)
    private String giLevel;

    @Column(name = "image_uri", length = 500)
    private String imageUri;

    @OneToMany(mappedBy = "recipe", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<RecipeIngredient> ingredients = new ArrayList<>();

    @OneToMany(mappedBy = "recipe", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<RecipeStep> steps = new ArrayList<>();

    protected Recipe() {
    }

    public Recipe(String title, String description, String category, String dishType,
                  String difficulty, Integer cookingTime, Integer servings,
                  BigDecimal totalCalories, String totalNutrients, String giLevel, String imageUri) {
        this.title = title;
        this.description = description;
        this.category = category;
        this.dishType = dishType;
        this.difficulty = difficulty;
        this.cookingTime = cookingTime;
        this.servings = servings;
        this.totalCalories = totalCalories;
        this.totalNutrients = totalNutrients;
        this.giLevel = giLevel;
        this.imageUri = imageUri;
    }

    public Recipe(String title, String externalId, String source, String description, String category, String dishType,
                  String difficulty, Integer cookingTime, Integer servings,
                  BigDecimal totalCalories, String totalNutrients, String giLevel, String imageUri) {
        this(title, description, category, dishType, difficulty, cookingTime, servings, totalCalories, totalNutrients, giLevel, imageUri);
        this.externalId = externalId;
        this.source = source;
    }

    public void addIngredient(RecipeIngredient ingredient) {
        ingredient.attach(this);
        this.ingredients.add(ingredient);
    }

    public void addStep(RecipeStep step) {
        step.attach(this);
        this.steps.add(step);
    }

    public void update(String title, String description, String category, String dishType,
                       String difficulty, Integer cookingTime, Integer servings,
                       BigDecimal totalCalories, String totalNutrients, String giLevel, String imageUri) {
        this.title = title;
        this.description = description;
        this.category = category;
        this.dishType = dishType;
        this.difficulty = difficulty;
        this.cookingTime = cookingTime;
        this.servings = servings;
        this.totalCalories = totalCalories;
        this.totalNutrients = totalNutrients;
        this.giLevel = giLevel;
        this.imageUri = imageUri;
    }

    public void replaceIngredients(List<RecipeIngredient> ingredients) {
        this.ingredients.clear();
        ingredients.forEach(this::addIngredient);
    }

    public void replaceSteps(List<RecipeStep> steps) {
        this.steps.clear();
        steps.forEach(this::addStep);
    }

    public void updateGiLevel(String giLevel) {
        this.giLevel = giLevel;
    }

    public Long getRecipeId() { return recipeId; }
    public String getTitle() { return title; }
    public String getExternalId() { return externalId; }
    public String getSource() { return source; }
    public String getDescription() { return description; }
    public String getCategory() { return category; }
    public String getDishType() { return dishType; }
    public String getDifficulty() { return difficulty; }
    public Integer getCookingTime() { return cookingTime; }
    public Integer getServings() { return servings; }
    public BigDecimal getTotalCalories() { return totalCalories; }
    public String getTotalNutrients() { return totalNutrients; }
    public String getGiLevel() { return giLevel; }
    public String getImageUri() { return imageUri; }
    public List<RecipeIngredient> getIngredients() { return ingredients; }
    public List<RecipeStep> getSteps() { return steps; }
}
