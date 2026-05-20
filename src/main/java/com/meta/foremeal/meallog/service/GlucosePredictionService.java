package com.meta.foremeal.meallog.service;

import com.meta.foremeal.meallog.api.dto.PredictionRequest;
import com.meta.foremeal.meallog.api.dto.PredictionResponse;
import com.meta.foremeal.meallog.domain.DailyVitalSummary;
import com.meta.foremeal.meallog.domain.GlucosePrediction;
import com.meta.foremeal.meallog.repo.DailyVitalSummaryRepository;
import com.meta.foremeal.meallog.repo.GlucosePredictionRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Service
public class GlucosePredictionService {

    private final RestTemplate restTemplate;
    private final GlucosePredictionRepository glucosePredictionRepository;
    private final DailyVitalSummaryRepository dailyVitalSummaryRepository;

    @Value("${foremeal.ai-server-url:http://localhost:8000}")
    private String aiServerUrl;

    public GlucosePredictionService(RestTemplate restTemplate,
                                    GlucosePredictionRepository glucosePredictionRepository,
                                    DailyVitalSummaryRepository dailyVitalSummaryRepository) {
        this.restTemplate = restTemplate;
        this.glucosePredictionRepository = glucosePredictionRepository;
        this.dailyVitalSummaryRepository = dailyVitalSummaryRepository;
    }

    @Transactional
    public PredictionResponse predictAndSave(PredictionRequest request) {
        PredictionResponse response = restTemplate.postForObject(
                aiServerUrl + "/predict",
                request,
                PredictionResponse.class
        );

        if (response == null) {
            throw new IllegalStateException("Python prediction server returned empty response.");
        }

        Long foodId = resolveFoodId(request);
        Integer pred1hMgdl = resolvePred1hMgdl(response.getPredictionCurve());

        GlucosePrediction savedPrediction = glucosePredictionRepository.save(
                new GlucosePrediction(
                        request.getMealId(),
                        request.getUserId(),
                        foodId,
                        "python-rule-v1",
                        pred1hMgdl,
                        response.getRiskLevel()
                )
        );

        Map<String, Object> graphData = Map.of(
                "curve", toGraphPoints(response.getPredictionCurve()),
                "riskLevel", response.getRiskLevel(),
                "predictedPeak", response.getPredictedPeak()
        );

        DailyVitalSummary savedSummary = dailyVitalSummaryRepository.save(
                new DailyVitalSummary(
                        BigDecimal.valueOf(response.getPredictedPeak()),
                        graphData,
                        savedPrediction.getPredictionId(),
                        request.getMealId(),
                        request.getUserId(),
                        foodId
                )
        );

        response.setPredictionId(savedPrediction.getPredictionId());
        response.setDailyVitalSummaryId(savedSummary.getPredId());

        return response;
    }

    public List<DailyVitalSummary> getGraphData(Long userId) {
        return dailyVitalSummaryRepository.findByUserIdOrderByPredIdDesc(userId);
    }

    private Long resolveFoodId(PredictionRequest request) {
        if (request.getFoodId() != null) {
            return request.getFoodId();
        }
        if (request.getFoods() != null && !request.getFoods().isEmpty()) {
            return request.getFoods().get(0).getFoodId();
        }
        return null;
    }

    private Integer resolvePred1hMgdl(List<Map<String, Object>> curve) {
        if (curve == null || curve.isEmpty()) {
            return null;
        }
        int index = Math.min(2, curve.size() - 1);
        Object value = curve.get(index).get("glucoseMgdl");
        if (value instanceof Number number) {
            return (int) Math.round(number.doubleValue());
        }
        return null;
    }

    private List<Map<String, Object>> toGraphPoints(List<Map<String, Object>> curve) {
        if (curve == null) {
            return List.of();
        }
        return curve;
    }
}
