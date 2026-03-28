package com.meta.foremeal.Controller;

import com.meta.foremeal.Entity.FoodMaster;
import com.meta.foremeal.Service.FoodService;
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
    public FoodMaster addFood(@RequestBody FoodMaster food){
        return foodService.saveFood(food);
    }

    //데이터 조회
    public List<FoodMaster> listFoods(){
        return foodService.getAllFoods();
    }
}
