package com.meta.foremeal.recommendation.api;

import com.meta.foremeal.global.security.principal.CustomUserPrincipal;
import com.meta.foremeal.recommendation.dto.RecommendationDto;
import com.meta.foremeal.recommendation.service.RecommendationService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/recommendations")
public class RecommendationController {

    private final RecommendationService recommendationService;

    public RecommendationController(RecommendationService recommendationService) {
        this.recommendationService = recommendationService;
    }

    @GetMapping("/recipes")
    public List<RecommendationDto.RecipeRecommendationResponse> recommendRecipes(
            @AuthenticationPrincipal CustomUserPrincipal principal,
            @RequestParam(required = false) String date,
            @RequestParam(defaultValue = "5") int limit
    ) {
        LocalDate targetDate = date == null ? LocalDate.now() : LocalDate.parse(date);
        return recommendationService.recommendRecipes(principal.getUserId(), targetDate, limit);
    }
}
