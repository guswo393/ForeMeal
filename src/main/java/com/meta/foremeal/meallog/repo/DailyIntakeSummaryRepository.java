package com.meta.foremeal.meallog.repo;

import com.meta.foremeal.meallog.domain.DailyIntakeSummary;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.Optional;

public interface DailyIntakeSummaryRepository extends JpaRepository<DailyIntakeSummary, Long> {
    Optional<DailyIntakeSummary> findByUserIdAndSummaryDate(Long userId, LocalDate summaryDate);
}
