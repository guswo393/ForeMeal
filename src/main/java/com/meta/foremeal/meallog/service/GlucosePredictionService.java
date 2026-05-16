package com.meta.foremeal.meallog.service;

import com.meta.foremeal.meallog.api.dto.PredictionRequest;
import com.meta.foremeal.meallog.api.dto.PredictionResponse;
import com.meta.foremeal.meallog.domain.GlucosePrediction;
import com.meta.foremeal.meallog.repo.GlucosePredictionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;

@Service
@RequiredArgsConstructor
public class GlucosePredictionService {
    private final RestTemplate restTemplate;
    private final GlucosePredictionRepository repository;

    @Value("${foremeal.ai-server-url:http://localhost:8000}")
    private String aiServerUrl;

    public PredictionResponse predictAndSave(PredictionRequest request) {
        return restTemplate.postForObject(
                aiServerUrl + "/predict/glucose",
                request,
                PredictionResponse.class
        );
    }

    public List<GlucosePrediction> getRecentPredictions(Long userId) {
        return repository.findTop20ByUserIdOrderByCreatedAtDesc(userId);
    }
}
