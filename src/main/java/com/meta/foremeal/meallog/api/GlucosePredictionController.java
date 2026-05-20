package com.meta.foremeal.meallog.api;

import com.meta.foremeal.global.security.principal.CustomUserPrincipal;
import com.meta.foremeal.meallog.api.dto.PredictionRequest;
import com.meta.foremeal.meallog.api.dto.PredictionResponse;
import com.meta.foremeal.meallog.domain.DailyVitalSummary;
import com.meta.foremeal.meallog.service.GlucosePredictionService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/predict")
public class GlucosePredictionController {

    private final GlucosePredictionService glucosePredictionService;

    public GlucosePredictionController(GlucosePredictionService glucosePredictionService) {
        this.glucosePredictionService = glucosePredictionService;
    }

    @PostMapping("/glucose")
    public PredictionResponse predictGlucose(@AuthenticationPrincipal CustomUserPrincipal principal,
                                             @RequestBody PredictionRequest request) {
        if (principal == null) {
            throw new IllegalArgumentException("Login is required to predict glucose.");
        }
        request.setUserId(principal.getUserId());
        return glucosePredictionService.predictAndSave(request);
    }

    @GetMapping("/glucose/graph")
    public List<DailyVitalSummary> getGraphData(@AuthenticationPrincipal CustomUserPrincipal principal) {
        if (principal == null) {
            throw new IllegalArgumentException("Login is required to read prediction graph data.");
        }
        return glucosePredictionService.getGraphData(principal.getUserId());
    }
}
