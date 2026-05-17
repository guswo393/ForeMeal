package com.meta.foremeal.meallog.repo;

import com.meta.foremeal.meallog.domain.DailyVitalSummary;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DailyVitalSummaryRepository extends JpaRepository<DailyVitalSummary, Long> {
    List<DailyVitalSummary> findByUserIdOrderByPredIdDesc(Long userId);
}