package com.meta.foremeal.FoodMaster;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface FoodMasterRepository extends JpaRepository<FoodMasterEntity, Long> {
    List<FoodMasterEntity> findByFoodNameContaining(String foodName);

    List<FoodMasterEntity> findByCategory(String category);
}
