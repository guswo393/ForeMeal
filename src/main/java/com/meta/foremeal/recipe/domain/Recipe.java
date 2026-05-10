package com.meta.foremeal.recipe.domain;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.util.List;

@Entity
@Table(name = "Recipe")
@Getter @Setter
@NoArgsConstructor
public class Recipe {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "recipe_id")
    private Long recipeId;

    private String title; // 레시피명칭

    @Column(columnDefinition = "TEXT")
    private String description; // 소개

    private String category; // 카테고리 (저염, 당뇨식 등)

    @Column(name = "dish_type")
    private String dishType; // 요리유형

    private String difficulty; // 난이도

    @Column(name = "cooking_time")
    private Integer cookingTime; // 소요시간 (INT)

    private Integer servings; // 기준인분 (INT)

    @Column(name = "total_calories")
    private BigDecimal totalCalories; // 총칼로리 (DECIMAL)

    @Column(name = "total_nutrients", columnDefinition = "JSON")
    private String totalNutrients; // 영양총량 (JSON 타입은 String으로 받거나 설정 필요)

    @Column(name = "gl_level")
    private String glLevel; // 혈당등급

    @OneToMany(mappedBy = "recipe", cascade = CascadeType.ALL)
    private List<RecipeSteps> steps;

    @OneToMany(mappedBy = "recipe", cascade = CascadeType.ALL)
    private List<RecipeTag> tags;

    @OneToMany(mappedBy = "recipe", cascade = CascadeType.ALL)
    private List<RecipeIngredients> ingredients;
}