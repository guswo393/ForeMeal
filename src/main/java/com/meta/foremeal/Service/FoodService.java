package com.meta.foremeal.Service;

import com.meta.foremeal.Entity.FoodMaster;
import com.meta.foremeal.Repository.FoodMasterRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor

public class FoodService {
    private final FoodMasterRepository foodRepository;

    //데이터 저장 (create)
    public FoodMaster saveFood(FoodMaster food){
        return foodRepository.save(food);
    }

    //전체 조회(read)
    public List<FoodMaster> getAllFoods(){
        return foodRepository.findAll();
    }
}

