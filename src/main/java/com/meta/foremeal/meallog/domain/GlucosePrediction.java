package com.meta.foremeal.meallog.domain;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class GlucosePrediction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 어떤 사용자의 데이터인지 식별
    @Column(nullable = false)
    private Long userId;

    // Python 모델이 예측한 최고 혈당치
    private Double predictedPeak;

    // 위험도 상태 (정상, 주의, 위험 등)
    private String riskLevel;

    // 식후 2시간 혈당 곡선 (데이터가 많으므로 길게 설정)
    // List 형태를 JSON 문자열로 변환해서 저장하기 위해 Lob 혹은 length 지정
    @Column(columnDefinition = "TEXT")
    private String predictionCurve;

    // 예측이 생성된 시간
    private LocalDateTime createdAt;

    // 생성 시 자동으로 시간을 기록하기 위한 설정
    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
    }
}