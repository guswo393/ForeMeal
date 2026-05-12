package com.meta.foremeal.recipe.parser;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class RecipeIngredientParserTest {

    private final RecipeIngredientParser parser = new RecipeIngredientParser();

    @Test
    void parsesIngredientsWithQuantitiesAndUnits() {
        List<ParsedIngredient> ingredients = parser.parse("chicken breast 100g, lettuce 50g, tomato 1개");

        assertThat(ingredients).hasSize(3);
        assertThat(ingredients.get(0).ingredientName()).isEqualTo("chicken breast");
        assertThat(ingredients.get(0).quantity()).isEqualByComparingTo("100");
        assertThat(ingredients.get(0).unit()).isEqualTo("g");
        assertThat(ingredients.get(1).ingredientName()).isEqualTo("lettuce");
        assertThat(ingredients.get(1).quantity()).isEqualByComparingTo("50");
        assertThat(ingredients.get(1).unit()).isEqualTo("g");
        assertThat(ingredients.get(2).ingredientName()).isEqualTo("tomato");
        assertThat(ingredients.get(2).quantity()).isEqualByComparingTo("1");
        assertThat(ingredients.get(2).unit()).isEqualTo("개");
    }

    @Test
    void returnsEmptyListWhenNoQuantityPatternExists() {
        List<ParsedIngredient> ingredients = parser.parse("ingredients are listed as plain text only");

        assertThat(ingredients).isEmpty();
    }
}
