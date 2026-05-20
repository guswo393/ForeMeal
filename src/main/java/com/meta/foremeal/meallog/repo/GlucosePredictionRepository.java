package com.meta.foremeal.meallog.repo;

import com.meta.foremeal.meallog.domain.GlucosePrediction;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface GlucosePredictionRepository extends JpaRepository<GlucosePrediction, Long> {
    List<GlucosePrediction> findByUserIdOrderByCreatedAtDesc(Long userId);
}