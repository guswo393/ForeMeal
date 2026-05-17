package com.meta.foremeal.meallog.api.dto;

import java.util.List;

public class PredictionResponse {
    private Long predictionId;
    private Long dailyVitalSummaryId;
    private Double predictedPeak;
    private List<Double> predictionCurve;
    private String riskLevel;

    public Long getPredictionId() { return predictionId; }
    public Long getDailyVitalSummaryId() { return dailyVitalSummaryId; }
    public Double getPredictedPeak() { return predictedPeak; }
    public List<Double> getPredictionCurve() { return predictionCurve; }
    public String getRiskLevel() { return riskLevel; }

    public void setPredictionId(Long predictionId) { this.predictionId = predictionId; }
    public void setDailyVitalSummaryId(Long dailyVitalSummaryId) { this.dailyVitalSummaryId = dailyVitalSummaryId; }
}