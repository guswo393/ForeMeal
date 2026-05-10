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
    private Double predictedPeak;      // 예상 피크 혈당
    private String riskLevel;          // 위험도 레벨 (정상, 주의, 위험)
    private List<Double> predictionCurve; // 식후 2시간 예측 곡선 데이터
    private String evidenceSummary;    // 예측 근거 요약
}