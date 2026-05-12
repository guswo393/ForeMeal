package com.meta.foremeal.foodmaster.external;

import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@JsonIgnoreProperties(ignoreUnknown = true)
public class FoodSafetyFoodDto {

    @JsonProperty("response")
    private Response response;

    @JsonProperty("I2790")
    private FoodNutrition foodNutrition;

    public List<Row> rows() {
        if (response != null && response.body != null && response.body.items != null) {
            return response.body.items.rows();
        }

        if (foodNutrition == null || foodNutrition.row == null) {
            return List.of();
        }
        return foodNutrition.row;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Response {
        private Body body;

        public Body getBody() {
            return body;
        }

        public void setBody(Body body) {
            this.body = body;
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Body {
        private Items items;

        public Items getItems() {
            return items;
        }

        public void setItems(Items items) {
            this.items = items;
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Items {
        @JsonFormat(with = JsonFormat.Feature.ACCEPT_SINGLE_VALUE_AS_ARRAY)
        private List<Row> item = new ArrayList<>();

        public List<Row> getItem() {
            return item;
        }

        public void setItem(List<Row> item) {
            this.item = item;
        }

        public List<Row> rows() {
            return item == null ? List.of() : item;
        }
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
        public String foodName() { return firstValue("FOOD_NM_KR", "DESC_KOR", "FOOD_NAME"); }
        public String category() { return firstValue("FOOD_CAT1_NM", "GROUP_NAME", "CATEGORY"); }
        public String makerName() { return firstValue("MAKER_NM", "MAKER_NAME"); }
        public String researchYear() { return firstValue("RESEARCH_YMD", "RESEARCH_YEAR"); }
        public String subRefName() { return firstValue("SUB_REF_NAME"); }
        public String servingSize() { return firstValue("SERVING_SIZE", "NUTR_STANDARD_AMOUNT"); }
        public String servingUnit() { return firstValue("SERVING_UNIT"); }
        public String calories() { return firstValue("AMT_NUM1", "NUTR_CONT1", "ENERGY", "ENERC"); }
        public String carbs() { return firstValue("AMT_NUM7", "NUTR_CONT2", "CARBOHYDRATE", "CHOCDF"); }
        public String protein() { return firstValue("AMT_NUM3", "NUTR_CONT3", "PROTEIN", "PROTC"); }
        public String fat() { return firstValue("AMT_NUM4", "NUTR_CONT4", "FAT", "FATCE"); }
        public String sugar() { return firstValue("AMT_NUM8", "NUTR_CONT5", "SUGAR", "SUGAR_TOTAL"); }
        public String sodium() { return firstValue("AMT_NUM14", "NUTR_CONT6", "NATRIUM", "NA"); }
        public String cholesterol() { return firstValue("AMT_NUM24", "NUTR_CONT7", "CHOLESTEROL", "CHOL"); }
        public String saturatedFat() { return firstValue("AMT_NUM23", "NUTR_CONT8", "SATURATED_FAT", "FASAT"); }
        public String transFat() { return firstValue("AMT_NUM25", "NUTR_CONT9", "TRANS_FAT", "FATRN"); }

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
