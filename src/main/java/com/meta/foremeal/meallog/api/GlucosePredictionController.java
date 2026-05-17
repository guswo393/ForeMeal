package com.meta.foremeal.meallog.api;

import com.meta.foremeal.meallog.api.dto.PredictionRequest;
import com.meta.foremeal.meallog.api.dto.PredictionResponse;
import com.meta.foremeal.meallog.domain.DailyVitalSummary;
import com.meta.foremeal.meallog.service.GlucosePredictionService;
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
    public PredictionResponse predictGlucose(@RequestBody PredictionRequest request) {
        return glucosePredictionService.predictAndSave(request);
    }

    @GetMapping("/glucose/graph")
    public List<DailyVitalSummary> getGraphData(@RequestParam Long userId) {
        return glucosePredictionService.getGraphData(userId);
    }
}