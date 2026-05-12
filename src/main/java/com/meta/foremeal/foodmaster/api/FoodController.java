package com.meta.foremeal.foodmaster.api;

import com.meta.foremeal.foodmaster.dto.FoodDto;
import com.meta.foremeal.foodmaster.service.FoodService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/foods")
@RequiredArgsConstructor
public class FoodController {
    private final FoodService foodService;

    //식품 검색 (/api/foods/search?name=닭가습살)
    @GetMapping("/search")
    public ResponseEntity<List<FoodDto.Response>> search(@RequestParam String name) {
        return ResponseEntity.ok(foodService.searchFoods(name));
    }

    //식품 상세 조회
    @GetMapping("/{id}")
    public ResponseEntity<FoodDto.Response> getDetail(@PathVariable Long id) {
        return ResponseEntity.ok(foodService.getFoodDetail(id));
    }

    //식품의약품안전처 식품영양성분 DB에서 식품 정보 가져오기
    @PostMapping("/import/food-safety")
    public ResponseEntity<FoodDto.ImportResponse> importFoodSafetyFoods(
            @RequestParam String name,
            @RequestParam(defaultValue = "1") int start,
            @RequestParam(defaultValue = "20") int end
    ) {
        return ResponseEntity.ok(foodService.importFoodsFromFoodSafety(name, start, end));
    }
}
