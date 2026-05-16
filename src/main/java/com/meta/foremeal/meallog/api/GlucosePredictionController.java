package com.meta.foremeal.meallog.api;

import com.meta.foremeal.meallog.api.dto.PredictionRequest;
import com.meta.foremeal.meallog.api.dto.PredictionResponse;
import com.meta.foremeal.meallog.domain.GlucosePrediction;
import com.meta.foremeal.meallog.service.GlucosePredictionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/predict")
@RequiredArgsConstructor
public class GlucosePredictionController {

    private final GlucosePredictionService glucosePredictionService;

    @PostMapping("/glucose")
    public ResponseEntity<PredictionResponse> predictGlucose(@RequestBody PredictionRequest request) {
        PredictionResponse response = glucosePredictionService.predictAndSave(request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/glucose/users/{userId}")
    public ResponseEntity<List<GlucosePrediction>> getRecentPredictions(@PathVariable Long userId) {
        return ResponseEntity.ok(glucosePredictionService.getRecentPredictions(userId));
    }
}
