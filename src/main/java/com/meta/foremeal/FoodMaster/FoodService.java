package com.meta.foremeal.FoodMaster;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor

public class FoodService {
    private final FoodMasterRepository foodRepository;

    //데이터 저장 (create)
    public FoodMasterEntity saveFood(FoodMasterEntity food){
        return foodRepository.save(food);
    }

    //전체 조회(read)
    public List<FoodMasterEntity> getAllFoods(){
        return foodRepository.findAll();
    }
}

