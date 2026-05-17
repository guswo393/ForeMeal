package com.meta.foremeal.foodmaster.external;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class FoodSafetyFoodDtoTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void parsesFoodNutritionDbInfoResponse() throws Exception {
        String json = """
                {
                  "response": {
                    "body": {
                      "items": {
                        "item": [
                          {
                            "FOOD_CD": "F001",
                            "FOOD_NM_KR": "닭가슴살",
                            "FOOD_CAT1_NM": "육류",
                            "MAKER_NM": "테스트식품",
                            "RESEARCH_YMD": "2025-01-23",
                            "SUB_REF_NAME": "식품의약품안전처",
                            "SERVING_SIZE": "100",
                            "SERVING_UNIT": "g",
                            "AMT_NUM1": "110",
                            "AMT_NUM7": "0",
                            "AMT_NUM3": "23",
                            "AMT_NUM4": "1.5",
                            "AMT_NUM8": "0",
                            "AMT_NUM14": "55",
                            "AMT_NUM24": "60",
                            "AMT_NUM23": "0.4",
                            "AMT_NUM25": "0"
                          }
                        ]
                      }
                    }
                  }
                }
                """;

        FoodSafetyFoodDto response = objectMapper.readValue(json, FoodSafetyFoodDto.class);

        assertThat(response.rows()).hasSize(1);
        FoodSafetyFoodDto.Row row = response.rows().get(0);
        assertThat(row.foodCode()).isEqualTo("F001");
        assertThat(row.foodName()).isEqualTo("닭가슴살");
        assertThat(row.category()).isEqualTo("육류");
        assertThat(row.makerName()).isEqualTo("테스트식품");
        assertThat(row.researchYear()).isEqualTo("2025-01-23");
        assertThat(row.subRefName()).isEqualTo("식품의약품안전처");
        assertThat(row.servingSize()).isEqualTo("100");
        assertThat(row.servingUnit()).isEqualTo("g");
        assertThat(row.calories()).isEqualTo("110");
        assertThat(row.carbs()).isEqualTo("0");
        assertThat(row.protein()).isEqualTo("23");
        assertThat(row.fat()).isEqualTo("1.5");
        assertThat(row.sugar()).isEqualTo("0");
        assertThat(row.sodium()).isEqualTo("55");
        assertThat(row.cholesterol()).isEqualTo("60");
        assertThat(row.saturatedFat()).isEqualTo("0.4");
        assertThat(row.transFat()).isEqualTo("0");
    }
}
