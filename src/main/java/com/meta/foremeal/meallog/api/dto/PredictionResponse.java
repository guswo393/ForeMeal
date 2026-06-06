package com.meta.foremeal.meallog.api.dto;

import java.util.List;
import java.util.Map;

public class PredictionResponse {
    private Long predictionId;
    private Long dailyVitalSummaryId;
    private Double baseGlucose;
    private Double predictedPeak;
    private List<Map<String, Object>> predictionCurve;
    private String riskLevel;
    private String evidenceSummary;

    public Long getPredictionId() { return predictionId; }
    public Long getDailyVitalSummaryId() { return dailyVitalSummaryId; }
    public Double getBaseGlucose() { return baseGlucose; }
    public Double getPredictedPeak() { return predictedPeak; }
    public List<Map<String, Object>> getPredictionCurve() { return predictionCurve; }
    public String getRiskLevel() { return riskLevel; }
    public String getEvidenceSummary() { return evidenceSummary; }

    public void setPredictionId(Long predictionId) { this.predictionId = predictionId; }
    public void setDailyVitalSummaryId(Long dailyVitalSummaryId) { this.dailyVitalSummaryId = dailyVitalSummaryId; }
    public void setBaseGlucose(Double baseGlucose) { this.baseGlucose = baseGlucose; }

    public void setPredictionCurve(List<Map<String, Object>> predictionCurve) { this.predictionCurve = predictionCurve; }
    public void setPredictedPeak(Double predictedPeak) { this.predictedPeak = predictedPeak; }
    public void setRiskLevel(String riskLevel) { this.riskLevel = riskLevel; }
    public void setEvidenceSummary(String evidenceSummary) { this.evidenceSummary = evidenceSummary; }
}
