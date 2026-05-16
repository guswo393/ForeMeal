package com.meta.foremeal.pantry.repository;

import com.meta.foremeal.pantry.domain.PantryScan;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface PantryScanRepository extends JpaRepository<PantryScan, Long>{

    // 특정 유저가 촬영한 스캔 히스토리를 최신순으로 정렬해서 보기
    List<PantryScan> findByUser_UserIdOrderByScannedAtDesc(Long userId);

    // 현재 AI가 분석 중(PROCESSING)인 스캔 작업이 있는지 상태 추적
    List<PantryScan> findByUser_UserIdAndStatus(Long userId, String status);
}