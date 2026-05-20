package com.meta.foremeal.meallog.domain;

import jakarta.persistence.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;
import java.util.Map;

@Entity
@Table(name = "daily_vital_summary")
public class DailyVitalSummary {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "pred_id")
    private Long predId;

    @Column(name = "expected_glucose_peak", precision = 6, scale = 2)
    private BigDecimal expectedGlucosePeak;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "simulation_curve_data", columnDefinition = "jsonb")
    private Map<String, Object> simulationCurveData;

    @Column(name = "prediction_id")
    private Long predictionId;

    @Column(name = "meal_id")
    private Long mealId;

    @Column(name = "user_id")
    private Long userId;

    @Column(name = "food_id")
    private Long foodId;

    protected DailyVitalSummary() {
    }

    public DailyVitalSummary(BigDecimal expectedGlucosePeak,
                             Map<String, Object> simulationCurveData,
                             Long predictionId,
                             Long mealId,
                             Long userId,
                             Long foodId) {
        this.expectedGlucosePeak = expectedGlucosePeak;
        this.simulationCurveData = simulationCurveData;
        this.predictionId = predictionId;
        this.mealId = mealId;
        this.userId = userId;
        this.foodId = foodId;
    }

    public Long getPredId() {
        return predId;
    }

    public Map<String, Object> getSimulationCurveData() {
        return simulationCurveData;
    }
}