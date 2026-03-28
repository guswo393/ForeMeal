package com.meta.foremeal.FoodMaster;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/foods")
@RequiredArgsConstructor
public class FoodController {
    private final FoodService foodService;

    //데이터 저장
    @PostMapping
    public FoodMasterEntity addFood(@RequestBody FoodMasterEntity food){
        return foodService.saveFood(food);
    }

    //데이터 조회
    public List<FoodMasterEntity> listFoods(){
        return foodService.getAllFoods();
    }
}
