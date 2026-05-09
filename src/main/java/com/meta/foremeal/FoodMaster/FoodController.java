package com.meta.foremeal.FoodMaster;

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
    public ResponseEntity<List<FoodMasterEntity>> search(@RequestParam String name) {
        return ResponseEntity.ok(foodService.searchFoods(name));
    }

    //식품 상세 조회
    @GetMapping("/{id}")
    public ResponseEntity<FoodMasterEntity> getDetail(@PathVariable Long id) {
        return ResponseEntity.ok(foodService.getFoodDetail(id));
    }
}
