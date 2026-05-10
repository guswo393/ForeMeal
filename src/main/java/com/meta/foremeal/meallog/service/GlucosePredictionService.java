package com.meta.foremeal.meallog.service;

import com.meta.foremeal.meallog.api.dto.PredictionRequest;
import com.meta.foremeal.meallog.api.dto.PredictionResponse;
import com.meta.foremeal.meallog.domain.GlucosePrediction;
import com.meta.foremeal.meallog.repo.GlucosePredictionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
@RequiredArgsConstructor
public class GlucosePredictionService {
    private final RestTemplate restTemplate;
    private final GlucosePredictionRepository repository;

    public PredictionResponse predictAndSave(PredictionRequest request) {
        String pythonUrl = "http://localhost:8000/predict";

        // 1. Python 서버 연동
        PredictionResponse response = restTemplate.postForObject(pythonUrl, request, PredictionResponse.class);

        // 2. 결과 저장
        if (response != null) {
            GlucosePrediction entity = GlucosePrediction.builder()
                    .userId(request.getUserId())
                    .predictedPeak(response.getPredictedPeak())
                    .riskLevel(response.getRiskLevel())
                    .predictionCurve(response.getPredictionCurve().toString())
                    .build();
            repository.save(entity);
        }
        return response;
    }
}