package com.meta.foremeal.meallog.repo;

import com.meta.foremeal.meallog.domain.MealLogItem;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MealLogItemRepository extends JpaRepository<MealLogItem, Long> {
}
