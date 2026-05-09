package com.meta.foremeal.FoodMaster;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class FoodService {

    private final FoodMasterRepository foodRepository;

    public List<FoodMasterEntity> searchFoods(String name) {
        return foodRepository.findByFoodNameContaining(name);
    }

    public FoodMasterEntity getFoodDetail(Long id) {
        return foodRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Food not found. foodId=" + id));
    }

    public FoodMasterEntity saveFood(FoodMasterEntity food) {
        return foodRepository.save(food);
    }
}
