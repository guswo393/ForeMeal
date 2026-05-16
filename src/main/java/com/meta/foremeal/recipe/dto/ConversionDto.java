package com.meta.foremeal.recipe.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public class ConversionDto {

    public record IngredientResponse(
            String name
    ) {}

    public record SubstituteResponse(
            String name,
            BigDecimal ratio
    ) {}

    public record CalculateRequest(
            @NotBlank String ingredient,
            @NotBlank String substitute,
            @NotNull @DecimalMin(value = "0.01") BigDecimal gram
    ) {}

    public record CalculateResponse(
            String ingredient,
            String substitute,
            BigDecimal inputGram,
            BigDecimal convertedGram,
            String inputGramText,
            String convertedGramText,
            String resultText,
            String description
    ) {}
}
