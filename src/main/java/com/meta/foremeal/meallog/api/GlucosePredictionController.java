package com.meta.foremeal.meallog.api;

import com.meta.foremeal.meallog.api.dto.PredictionRequest;
import com.meta.foremeal.meallog.api.dto.PredictionResponse;
import com.meta.foremeal.meallog.service.GlucosePredictionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/predict")
@RequiredArgsConstructor
public class GlucosePredictionController {

    private final GlucosePredictionService glucosePredictionService;

    /**
     * [추가] 혈당 예측 요청을 받는 엔드포인트입니다.
     * Postman 주소: POST http://localhost:8080/api/v1/predict/glucose
     */
    @PostMapping("/glucose")
    public ResponseEntity<PredictionResponse> predictGlucose(@RequestBody PredictionRequest request) {
        // 서비스 호출 (AI 연동 + DB 저장)
        PredictionResponse response = glucosePredictionService.predictAndSave(request);
        return ResponseEntity.ok(response);
    }
}