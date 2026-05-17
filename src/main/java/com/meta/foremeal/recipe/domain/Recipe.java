package com.meta.foremeal.recipe.domain;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.util.List;

@Entity
public class Recipe {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "recipe_id")
    private Long recipeId;






    @Column(name = "cooking_time")







}