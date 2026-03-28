package com.meta.foremeal.health.repo;

import com.meta.foremeal.health.domain.BloodPressure;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface BloodPressureRepository extends JpaRepository<BloodPressure, Long> {
    List<BloodPressure> findByUserIdAndMeasuredAtBetweenOrderByMeasuredAtAsc(Long userId, LocalDateTime from, LocalDateTime to);
}
