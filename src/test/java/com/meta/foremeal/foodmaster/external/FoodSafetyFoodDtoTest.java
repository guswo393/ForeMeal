package com.meta.foremeal.foodmaster.external;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class FoodSafetyFoodDtoTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void parsesFoodSafetyI2790Response() throws Exception {
        String json = """
                {
                  "I2790": {
                    "row": [
                      {
                        "FOOD_CD": "F001",
                        "DESC_KOR": "닭가슴살",
                        "GROUP_NAME": "육류",
                        "NUTR_CONT1": "110",
                        "NUTR_CONT2": "0",
                        "NUTR_CONT3": "23",
                        "NUTR_CONT4": "1.5",
                        "NUTR_CONT5": "0",
                        "NUTR_CONT6": "55"
                      }
                    ]
                  }
                }
                """;

        FoodSafetyFoodDto response = objectMapper.readValue(json, FoodSafetyFoodDto.class);

        assertThat(response.rows()).hasSize(1);
        FoodSafetyFoodDto.Row row = response.rows().get(0);
        assertThat(row.foodCode()).isEqualTo("F001");
        assertThat(row.foodName()).isEqualTo("닭가슴살");
        assertThat(row.category()).isEqualTo("육류");
        assertThat(row.calories()).isEqualTo("110");
        assertThat(row.carbs()).isEqualTo("0");
        assertThat(row.protein()).isEqualTo("23");
        assertThat(row.fat()).isEqualTo("1.5");
        assertThat(row.sugar()).isEqualTo("0");
        assertThat(row.sodium()).isEqualTo("55");
    }
}
