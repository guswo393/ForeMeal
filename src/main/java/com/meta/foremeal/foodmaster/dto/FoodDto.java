package com.meta.foremeal.foodmaster.dto;

import com.meta.foremeal.foodmaster.domain.FoodMasterEntity;

import java.util.List;

public class FoodDto {

    public record Response(
            Long foodId,
            String externalId,
            String source,
            String foodName,
            String category,
            String makerName,
            String researchYear,
            String subRefName,
            Double servingSize,
            String servingUnit,
            Double calories,
            Double carbs,
            Double protein,
            Double fat,
            Double sugar,
            Double sodium,
            Double cholesterol,
            Double saturatedFat,
            Double transFat,
            Double giIndex
    ) {
        public static Response from(FoodMasterEntity food) {
            return new Response(
                    food.getFoodId(),
                    food.getExternalId(),
                    food.getSource(),
                    food.getFoodName(),
                    food.getCategory(),
                    food.getMakerName(),
                    food.getResearchYear(),
                    food.getSubRefName(),
                    food.getServingSize(),
                    food.getServingUnit(),
                    food.getCalories(),
                    food.getCarbs(),
                    food.getProtein(),
                    food.getFat(),
                    food.getSugar(),
                    food.getSodium(),
                    food.getCholesterol(),
                    food.getSaturatedFat(),
                    food.getTransFat(),
                    food.getGiIndex()
            );
        }
    }

    public record ImportResponse(
            int fetched,
            int imported,
            int updated,
            int skipped
    ) {}

    public static List<Response> from(List<FoodMasterEntity> foods) {
        return foods.stream()
                .map(Response::from)
                .toList();
    }
}
