package com.meta.foremeal.meallog.domain;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "glucose_prediction")
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class GlucosePrediction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "prediction_id")
    private Long predictionId;

    @Column(name = "meal_id")
    private Long mealId;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "food_id")
    private Long foodId;

    @Column(name = "model_version", length = 50)
    private String modelVersion;

    @Column(name = "pred_1h_mgdl")
    private Integer pred1hMgdl;

    @Column(name = "risk_level", length = 20)
    private String riskLevel;

    @Column(name = "created_at", insertable = false, updatable = false)
    private LocalDateTime createdAt;
}
