package com.meta.foremeal.recipe.api;

import com.meta.foremeal.recipe.dto.ConversionDto;
import com.meta.foremeal.recipe.service.ConversionService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/recipe/conversions")
public class ConversionController {

    private final ConversionService conversionService;

    public ConversionController(ConversionService conversionService) {
        this.conversionService = conversionService;
    }

    @GetMapping("/ingredients")
    public List<ConversionDto.IngredientResponse> getIngredients() {
        return conversionService.getIngredients();
    }

    @GetMapping("/substitutes")
    public List<ConversionDto.SubstituteResponse> getSubstitutes(@RequestParam String ingredient) {
        return conversionService.getSubstitutes(ingredient);
    }

    @PostMapping("/calculate")
    public ConversionDto.CalculateResponse calculate(@RequestBody @Valid ConversionDto.CalculateRequest request) {
        return conversionService.calculate(request);
    }
}
