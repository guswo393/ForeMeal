package com.meta.foremeal.meallog.repo;

import com.meta.foremeal.meallog.domain.GlucosePrediction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface GlucosePredictionRepository extends JpaRepository<GlucosePrediction, Long> {
    List<GlucosePrediction> findTop20ByUserIdOrderByCreatedAtDesc(Long userId);
}
