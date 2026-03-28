package com.meta.foremeal.health.repo;

import com.meta.foremeal.health.domain.Glucose;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface GlucoseRepository extends JpaRepository<Glucose, Long> {
    List<Glucose> findByUserIdAndMeasuredAtBetweenOrderByMeasuredAtAsc(Long userId, LocalDateTime from, LocalDateTime to);
}
