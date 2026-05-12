package com.meta.foremeal.foodmaster.repo;

import com.meta.foremeal.foodmaster.domain.FoodMasterEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface FoodMasterRepository extends JpaRepository<FoodMasterEntity, Long> {
    //식품명에 검색어가 포함된 모든 식품 search
    List<FoodMasterEntity> findByFoodNameContaining(String foodName);
    //카테고리별 필터링
    List<FoodMasterEntity> findByCategory(String category);
    Optional<FoodMasterEntity> findBySourceAndExternalId(String source, String externalId);
}
