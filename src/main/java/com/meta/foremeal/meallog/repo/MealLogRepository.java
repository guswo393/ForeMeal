package com.meta.foremeal.meallog.repo;

import com.meta.foremeal.meallog.domain.MealLog;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface MealLogRepository extends JpaRepository<MealLog, Long> {
    List<MealLog> findByUserIdAndEatenAtBetweenOrderByEatenAtAsc(Long userId, LocalDateTime from, LocalDateTime to);
}
