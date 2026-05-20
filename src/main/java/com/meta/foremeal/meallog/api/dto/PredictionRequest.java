package com.meta.foremeal.meallog.api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PredictionRequest {
    private Long userId;
    private Long mealId;
    private Long foodId;
    private Double currentGlucose;
    private Integer systolicBp;
    private Integer diastolicBp;
    private String activityLevel;
    private List<FoodItemDto> foods;

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class FoodItemDto {
        private Long foodId;
        private String name;
        private Double calories;
        private Double carbs;
        private Double sugar;
        private Double sodium;
        private Double quantity;
    }
}
