package com.meta.foremeal.recipe.external;

import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@JsonIgnoreProperties(ignoreUnknown = true)
public class FoodSafetyRecipeDto {

    @JsonProperty("COOKRCP01")
    private CookRecipe cookRecipe;

    public List<Row> rows() {
        if (cookRecipe == null || cookRecipe.row == null) {
            return List.of();
        }
        return cookRecipe.row;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class CookRecipe {
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

        public String recipeSeq() { return value("RCP_SEQ"); }
        public String recipeName() { return value("RCP_NM"); }
        public String cookingWay() { return value("RCP_WAY2"); }
        public String category() { return value("RCP_PAT2"); }
        public String calories() { return value("INFO_ENG"); }
        public String carbs() { return value("INFO_CAR"); }
        public String protein() { return value("INFO_PRO"); }
        public String fat() { return value("INFO_FAT"); }
        public String sodium() { return value("INFO_NA"); }
        public String hashTag() { return value("HASH_TAG"); }
        public String mainImageUri() { return value("ATT_FILE_NO_MK") != null ? value("ATT_FILE_NO_MK") : value("ATT_FILE_NO_MAIN"); }
        public String ingredientInfo() { return value("RCP_PARTS_DTLS"); }
        public String manual(int order) { return value(String.format("MANUAL%02d", order)); }
        public String manualImage(int order) { return value(String.format("MANUAL_IMG%02d", order)); }
    }
}
