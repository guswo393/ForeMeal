package com.meta.foremeal.foodmaster.service;

import com.meta.foremeal.foodmaster.domain.FoodMasterEntity;
import com.meta.foremeal.foodmaster.dto.FoodDto;
import com.meta.foremeal.foodmaster.external.FoodSafetyFoodClient;
import com.meta.foremeal.foodmaster.external.FoodSafetyFoodDto;
import com.meta.foremeal.foodmaster.repo.FoodMasterRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor

public class FoodService {
    private static final String SOURCE_FOOD_SAFETY = "FOOD_SAFETY_KOREA";

    private final FoodMasterRepository foodRepository;
    private final FoodSafetyFoodClient foodSafetyFoodClient;

    //식품 검색
    @Transactional(readOnly = true)
    public List<FoodDto.Response> searchFoods(String name) {
        return FoodDto.from(foodRepository.findByFoodNameContaining(name));
    }

    //식품 상세 조회 (id 기준)
    @Transactional(readOnly = true)
    public FoodDto.Response getFoodDetail(Long id) {
        return foodRepository.findById(id)
                .map(FoodDto.Response::from)
                .orElseThrow(() -> new RuntimeException("요청한 식품 정보를 찾을 수 없습니다."));
    }

    //식약처 API로부터 받아온 데이터 저장
    @Transactional
    public FoodMasterEntity saveFood(FoodMasterEntity food) {

        return foodRepository.save(food);
    }

    @Transactional
    public FoodDto.ImportResponse importFoodsFromFoodSafety(String foodName, int start, int end) {
        FoodSafetyFoodDto response = foodSafetyFoodClient.fetchByName(foodName, start, end);
        List<FoodSafetyFoodDto.Row> rows = response == null ? List.of() : response.rows();

        int imported = 0;
        int updated = 0;
        int skipped = 0;

        for (FoodSafetyFoodDto.Row row : rows) {
            String externalId = row.foodCode();
            String name = row.foodName();

            if (name == null) {
                skipped++;
                continue;
            }

            FoodMasterEntity food = externalId == null
                    ? new FoodMasterEntity()
                    : foodRepository.findBySourceAndExternalId(SOURCE_FOOD_SAFETY, externalId)
                    .orElseGet(FoodMasterEntity::new);

            boolean isNew = food.getFoodId() == null;
            apply(row, food);
            foodRepository.save(food);

            if (isNew) {
                imported++;
            } else {
                updated++;
            }
        }

        return new FoodDto.ImportResponse(rows.size(), imported, updated, skipped);
    }

    private void apply(FoodSafetyFoodDto.Row row, FoodMasterEntity food) {
        food.setExternalId(row.foodCode());
        food.setSource(SOURCE_FOOD_SAFETY);
        food.setFoodName(row.foodName());
        food.setCategory(row.category());
        food.setMakerName(row.makerName());
        food.setResearchYear(row.researchYear());
        food.setSubRefName(row.subRefName());
        food.setServingSize(parseDouble(row.servingSize()));
        food.setServingUnit(row.servingUnit());
        food.setCalories(parseDouble(row.calories()));
        food.setCarbs(parseDouble(row.carbs()));
        food.setProtein(parseDouble(row.protein()));
        food.setFat(parseDouble(row.fat()));
        food.setSugar(parseDouble(row.sugar()));
        food.setSodium(parseDouble(row.sodium()));
        food.setCholesterol(parseDouble(row.cholesterol()));
        food.setSaturatedFat(parseDouble(row.saturatedFat()));
        food.setTransFat(parseDouble(row.transFat()));
    }

    private Double parseDouble(String value) {
        if (value == null) {
            return null;
        }

        try {
            String normalized = value.replaceAll("[^0-9.\\-]", "");
            return normalized.isBlank() ? null : Double.valueOf(normalized);
        } catch (NumberFormatException e) {
            return null;
        }
    }

}

