package com.meta.foremeal.FoodMaster;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface FoodMasterRepository extends JpaRepository<FoodMasterEntity, Long> {
    Optional<FoodMasterEntity> findByFoodName(String foodName);
}
