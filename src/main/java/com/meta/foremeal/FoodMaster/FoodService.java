package com.meta.foremeal.FoodMaster;

import lombok.RequiredArgsConstructor;
import lombok.Value;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor

public class FoodService {
    private final FoodMasterRepository foodRepository;

    /*
    //api key값
    @Value("${food.api.key}")
    private String apikey;

    @Value("${food.api.url}")
    private String baseUrl;

    //식약처 API 호출 메소드
    public void fetchFoodDataFromApi(String foodName) {
        String url = String.format("%s/%s/I2790/json/1/5/DESC_KOR=%s", baseUrl, apikey, foodName);

    }
    */

    //식품 검색
    public List<FoodMasterEntity> searchFoods(String name) {
        return foodRepository.findByFoodNameContaining(name);
    }

    //식품 상세 조회 (id 기준)
    public FoodMasterEntity getFoodDetail(Long id) {
        return foodRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("식품 정보가 없습니다."));
    }

    //식약처 API로부터 받아온 데이터 저장
    public FoodMasterEntity saveFood(FoodMasterEntity food) {

        return foodRepository.save(food);
    }
}

