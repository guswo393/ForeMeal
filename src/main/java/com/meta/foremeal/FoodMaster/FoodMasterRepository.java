package com.meta.foremeal.FoodMaster;

import com.meta.foremeal.FoodMaster.FoodMasterEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface FoodMasterRepository extends JpaRepository<FoodMasterEntity, Long> {

    // 완제품/식재료명으로 마스터 정보 단건 탐색
    Optional<FoodMasterEntity> findByFoodName(String foodName);

    // 당뇨 환자 위험 필터링: 기준 GI 지수 이상인 고위험 마스터 푸드 조회
    List<FoodMasterEntity> findByGiIndexGreaterThanEqual(Double giIndex);

    // 고혈압 환자 위험 필터링: 기준 나트륨 이상인 마스터 푸드 조회
    List<FoodMasterEntity> findBySodiumGreaterThanEqual(Double sodium);
}
