package com.meta.foremeal.foodmaster.service;

import com.meta.foremeal.foodmaster.domain.FoodMasterEntity;
import com.meta.foremeal.foodmaster.dto.FoodDto;
import com.meta.foremeal.foodmaster.external.FoodSafetyFoodClient;
import com.meta.foremeal.foodmaster.external.FoodSafetyFoodDto;
import com.meta.foremeal.foodmaster.repo.FoodMasterRepository;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class FoodServiceTest {

    private final FoodMasterRepository foodRepository = mock(FoodMasterRepository.class);
    private final FoodSafetyFoodClient foodSafetyFoodClient = mock(FoodSafetyFoodClient.class);
    private final FoodService foodService = new FoodService(foodRepository, foodSafetyFoodClient);

    @Test
    void importsFoodSafetyFoods() throws Exception {
        FoodSafetyFoodDto response = response(row("F001", "닭가슴살"));
        when(foodSafetyFoodClient.fetchByName("닭가슴살", 1, 1)).thenReturn(response);
        when(foodRepository.findBySourceAndExternalId("FOOD_SAFETY_KOREA", "F001")).thenReturn(Optional.empty());
        when(foodRepository.save(any(FoodMasterEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

        FoodDto.ImportResponse result = foodService.importFoodsFromFoodSafety("닭가슴살", 1, 1);

        assertThat(result.fetched()).isEqualTo(1);
        assertThat(result.imported()).isEqualTo(1);
        assertThat(result.updated()).isZero();
        assertThat(result.skipped()).isZero();

        FoodMasterEntity saved = captureSavedFood();
        assertThat(saved.getExternalId()).isEqualTo("F001");
        assertThat(saved.getSource()).isEqualTo("FOOD_SAFETY_KOREA");
        assertThat(saved.getFoodName()).isEqualTo("닭가슴살");
        assertThat(saved.getCategory()).isEqualTo("육류");
        assertThat(saved.getMakerName()).isEqualTo("테스트식품");
        assertThat(saved.getResearchYear()).isEqualTo("2025-01-23");
        assertThat(saved.getSubRefName()).isEqualTo("식품의약품안전처");
        assertThat(saved.getServingSize()).isEqualTo(100.0);
        assertThat(saved.getServingUnit()).isEqualTo("g");
        assertThat(saved.getCalories()).isEqualTo(110.0);
        assertThat(saved.getCarbs()).isEqualTo(0.0);
        assertThat(saved.getProtein()).isEqualTo(23.0);
        assertThat(saved.getFat()).isEqualTo(1.5);
        assertThat(saved.getSugar()).isEqualTo(0.0);
        assertThat(saved.getSodium()).isEqualTo(55.0);
        assertThat(saved.getCholesterol()).isEqualTo(60.0);
        assertThat(saved.getSaturatedFat()).isEqualTo(0.4);
        assertThat(saved.getTransFat()).isEqualTo(0.0);
    }

    private FoodMasterEntity captureSavedFood() {
        var foodCaptor = org.mockito.ArgumentCaptor.forClass(FoodMasterEntity.class);
        verify(foodRepository).save(foodCaptor.capture());
        return foodCaptor.getValue();
    }

    private FoodSafetyFoodDto response(FoodSafetyFoodDto.Row... rows) throws Exception {
        FoodSafetyFoodDto dto = new FoodSafetyFoodDto();
        FoodSafetyFoodDto.FoodNutrition foodNutrition = new FoodSafetyFoodDto.FoodNutrition();
        foodNutrition.setRow(List.of(rows));

        Field field = FoodSafetyFoodDto.class.getDeclaredField("foodNutrition");
        field.setAccessible(true);
        field.set(dto, foodNutrition);

        return dto;
    }

    private FoodSafetyFoodDto.Row row(String foodCode, String foodName) {
        FoodSafetyFoodDto.Row row = new FoodSafetyFoodDto.Row();
        row.put("FOOD_CD", foodCode);
        row.put("FOOD_NM_KR", foodName);
        row.put("FOOD_CAT1_NM", "육류");
        row.put("MAKER_NM", "테스트식품");
        row.put("RESEARCH_YMD", "2025-01-23");
        row.put("SUB_REF_NAME", "식품의약품안전처");
        row.put("SERVING_SIZE", "100");
        row.put("SERVING_UNIT", "g");
        row.put("AMT_NUM1", "110");
        row.put("AMT_NUM7", "0");
        row.put("AMT_NUM3", "23");
        row.put("AMT_NUM4", "1.5");
        row.put("AMT_NUM8", "0");
        row.put("AMT_NUM14", "55");
        row.put("AMT_NUM24", "60");
        row.put("AMT_NUM23", "0.4");
        row.put("AMT_NUM25", "0");
        return row;
    }
}
