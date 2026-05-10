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
    private Double currentGlucose;    // 현재 혈당값
    private List<FoodItemDto> foods;  // 선택한 음식 리스트 (영양성분 포함)
    private String activityLevel;     // 활동량 (가만히 있음, 가벼운 활동 등)

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class FoodItemDto {
        private String name;
        private Double carbs;         // 탄수화물
        private Double sugar;         // 당류
    }
}