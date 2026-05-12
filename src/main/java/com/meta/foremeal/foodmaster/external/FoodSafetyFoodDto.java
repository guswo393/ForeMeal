package com.meta.foremeal.foodmaster.external;

import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@JsonIgnoreProperties(ignoreUnknown = true)
public class FoodSafetyFoodDto {

    @JsonProperty("I2790")
    private FoodNutrition foodNutrition;

    public List<Row> rows() {
        if (foodNutrition == null || foodNutrition.row == null) {
            return List.of();
        }
        return foodNutrition.row;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class FoodNutrition {
        private List<Row> row = new ArrayList<>();

        public List<Row> getRow() {
            return row;
        }

        public void setRow(List<Row> row) {
            this.row = row;
        }
    }

    public static class Row {
        private final Map<String, String> values = new HashMap<>();

        @JsonAnySetter
        public void put(String key, Object value) {
            values.put(key, value == null ? null : String.valueOf(value));
        }

        public String value(String key) {
            String value = values.get(key);
            return value == null || value.isBlank() ? null : value.trim();
        }

        public String foodCode() { return firstValue("FOOD_CD", "NUM"); }
        public String foodName() { return firstValue("DESC_KOR", "FOOD_NM_KR", "FOOD_NAME"); }
        public String category() { return firstValue("GROUP_NAME", "FOOD_CAT1_NM", "CATEGORY"); }
        public String calories() { return firstValue("NUTR_CONT1", "AMT_NUM1", "ENERGY"); }
        public String carbs() { return firstValue("NUTR_CONT2", "AMT_NUM7", "CARBOHYDRATE"); }
        public String protein() { return firstValue("NUTR_CONT3", "AMT_NUM3", "PROTEIN"); }
        public String fat() { return firstValue("NUTR_CONT4", "AMT_NUM4", "FAT"); }
        public String sugar() { return firstValue("NUTR_CONT5", "AMT_NUM8", "SUGAR"); }
        public String sodium() { return firstValue("NUTR_CONT6", "AMT_NUM14", "NATRIUM"); }

        private String firstValue(String... keys) {
            for (String key : keys) {
                String value = value(key);
                if (value != null) {
                    return value;
                }
            }
            return null;
        }
    }
}
