package com.meta.foremeal.recipe.parser;

import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class RecipeIngredientParser {

    private static final Pattern INGREDIENT_PATTERN = Pattern.compile(
            "([가-힣A-Za-z0-9\\s·\\-_/]+?)\\s*(\\d+(?:\\.\\d+)?)\\s*(g|kg|ml|mL|L|개|큰술|작은술|컵|스푼|T|t|장|쪽|알|대|줌|꼬집|마리|봉|팩|통|줄|잎|근|그램|밀리리터)"
    );

    public List<ParsedIngredient> parse(String ingredientInfo) {
        if (ingredientInfo == null || ingredientInfo.isBlank()) {
            return List.of();
        }

        String normalized = normalize(ingredientInfo);
        Matcher matcher = INGREDIENT_PATTERN.matcher(normalized);
        List<ParsedIngredient> ingredients = new ArrayList<>();

        while (matcher.find()) {
            String name = cleanName(matcher.group(1));
            BigDecimal quantity = new BigDecimal(matcher.group(2));
            String unit = normalizeUnit(matcher.group(3));

            if (!name.isBlank()) {
                ingredients.add(new ParsedIngredient(name, quantity, unit));
            }
        }

        return ingredients;
    }

    private String normalize(String value) {
        return value
                .replaceAll("[\\[\\](){}]", " ")
                .replaceAll("[:：]", " ")
                .replaceAll("[,;\\n\\r]+", " ")
                .replaceAll("\\s+", " ")
                .trim();
    }

    private String cleanName(String value) {
        return value
                .replaceAll("(?i)재료|양념|소스|주재료|부재료", " ")
                .replaceAll("\\s+", " ")
                .trim();
    }

    private String normalizeUnit(String unit) {
        return switch (unit) {
            case "그램" -> "g";
            case "밀리리터" -> "ml";
            default -> unit;
        };
    }
}
