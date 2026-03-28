package com.meta.foremeal.Repository;

import com.meta.foremeal.Entity.FoodMaster;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface FoodMasterRepository extends JpaRepository<FoodMaster, Long> {
    Optional<FoodMaster> findByFoodName(String foodName);
}
