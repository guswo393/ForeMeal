package com.meta.foremeal.recipe.domain;

import jakarta.persistence.*;

import java.math.BigDecimal;

@Entity
@Table(name = "recipe_ingredients")
public class RecipeIngredient {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "item_id")
    private Long itemId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "recipe_id", nullable = false)
    private Recipe recipe;

    @Column(name = "food_id")
    private Long foodId;

    @Column(name = "ingredient_name", nullable = false, length = 255)
    private String ingredientName;

    @Column(name = "quantity", precision = 10, scale = 2)
    private BigDecimal quantity;

    @Column(name = "unit", length = 30)
    private String unit;

    protected RecipeIngredient() {
    }

    public RecipeIngredient(Long foodId, String ingredientName, BigDecimal quantity, String unit) {
        this.foodId = foodId;
        this.ingredientName = ingredientName;
        this.quantity = quantity;
        this.unit = unit;
    }

    void attach(Recipe recipe) {
        this.recipe = recipe;
    }

    public Long getItemId() { return itemId; }
    public Long getFoodId() { return foodId; }
    public String getIngredientName() { return ingredientName; }
    public BigDecimal getQuantity() { return quantity; }
    public String getUnit() { return unit; }
}
