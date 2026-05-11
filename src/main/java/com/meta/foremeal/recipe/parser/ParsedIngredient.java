package com.meta.foremeal.recipe.parser;

import java.math.BigDecimal;

public record ParsedIngredient(
        String ingredientName,
        BigDecimal quantity,
        String unit
) {
}
