package com.meta.foremeal.meallog.repo;

import com.meta.foremeal.meallog.domain.GlucosePrediction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * [추가] PostgreSQL 테이블과 매핑되어 CRUD를 담당합니다.
 * 팀 컨벤션에 따라 repo 패키지에 위치합니다.
 */
@Repository
public interface GlucosePredictionRepository extends JpaRepository<GlucosePrediction, Long> {
}