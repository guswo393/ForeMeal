package com.meta.foremeal.health.domain;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "glucose")
public class Glucose {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "glucose_id")
    private Long glucoseId;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "measured_at", nullable = false)
    private LocalDateTime measuredAt;

    @Column(name = "glucose_value", nullable = false, precision = 6, scale = 2)
    private BigDecimal glucoseValue;

    @Enumerated(EnumType.STRING)
    @Column(name = "measure_type", nullable = false, length = 30)
    private GlucoseMeasureType measureType;

    @Column(name = "memo", columnDefinition = "text")
    private String memo;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    protected Glucose() {
    }

    public Glucose(Long userId,
                   LocalDateTime measuredAt,
                   BigDecimal glucoseValue,
                   GlucoseMeasureType measureType,
                   String memo) {
        this.userId = userId;
        this.measuredAt = measuredAt;
        this.glucoseValue = glucoseValue;
        this.measureType = measureType;
        this.memo = memo;
    }

    public Long getGlucoseId() {
        return glucoseId;
    }

    public Long getUserId() {
        return userId;
    }

    public LocalDateTime getMeasuredAt() {
        return measuredAt;
    }

    public BigDecimal getGlucoseValue() {
        return glucoseValue;
    }

    public GlucoseMeasureType getMeasureType() {
        return measureType;
    }

    public String getMemo() {
        return memo;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}