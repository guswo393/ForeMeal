package com.meta.foremeal.meallog.api.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class PredictionResponse {
    private Long predictionId;
    private Long dailySummaryId;
    private Long userId;
    private Long mealId;
    private Long foodId;
    private String modelVersion;
    private Integer pred1hMgdl;
    private Double predictedPeak;
    private Integer predictedSystolicBp;
    private Integer predictedDiastolicBp;
    private String riskLevel;
    private List<Map<String, Object>> predictionCurve;
    private String evidenceSummary;
}
