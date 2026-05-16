package com.meta.foremeal.Pantry.repository;

import com.meta.foremeal.Pantry.domain.PantryItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

public interface PantryItemRepository extends JpaRepository<PantryItem, Long>{

    // 1. 특정 사용자의 냉장고에 있는 모든 재료 조회 (마스터 영양소 데이터가 있다면 페치 조인으로 한 번에 가져옴)
    @Query("select i from Ingredient i left join fetch i.foodMaster where i.userId = :userId")
    List<PantryItem> findAllByUserIdWithFoodMaster(@Param("userId") Long userId);

    // 2. 특정 사진 스캔(scanId) 이력을 통해 냉장고에 대량 등록된 재료 목록 조회
    @Query("select i from Ingredient i where i.pantryScan.scanId = :scanId")
    List<PantryItem> findByPantryScanId(@Param("scanId") Long scanId);

    // 3. 유통기한이 지난 냉장고 재료들만 골라내기 (알림 보낼 때 사용)
    @Query("select i from Ingredient i where i.userId = :userId and i.expirationDate < current_date")
    List<PantryItem> findExpiredIngredients(@Param("userId") Long userId);
}
