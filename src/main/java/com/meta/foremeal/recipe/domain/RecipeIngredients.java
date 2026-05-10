package com.meta.foremeal.recipe.domain;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;

@Entity
@Table(name = "RecipeIngredients")
@Getter @Setter
@NoArgsConstructor
public class RecipeIngredients {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "item_id")
    private Long itemId; // 항목고유 ID

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "recipe_id")
    private Recipe Recipe;

    @Column(name = "food_id")
    private Long foodId; // Food ID (외부 연결용)

    @Column(name = "ingredient_name")
    private String ingredientName; // 재료명

    private BigDecimal quantity; // 양 (DECIMAL)

    private String unit; // 단위
}