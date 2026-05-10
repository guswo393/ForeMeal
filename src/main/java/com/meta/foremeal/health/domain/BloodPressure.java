package com.meta.foremeal.health.domain;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "blood_pressure")
public class BloodPressure {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "blood_pressure_id")
    private Long bloodPressureId;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "measured_at", nullable = false)
    private LocalDateTime measuredAt;

    @Column(name = "systolic", nullable = false)
    private Integer systolic;

    @Column(name = "diastolic", nullable = false)
    private Integer diastolic;

    @Column(name = "pulse")
    private Integer pulse;

    @Column(name = "memo", columnDefinition = "text")
    private String memo;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    protected BloodPressure() {
    }

    public BloodPressure(Long userId,
                         LocalDateTime measuredAt,
                         Integer systolic,
                         Integer diastolic,
                         Integer pulse,
                         String memo) {
        this.userId = userId;
        this.measuredAt = measuredAt;
        this.systolic = systolic;
        this.diastolic = diastolic;
        this.pulse = pulse;
        this.memo = memo;
    }

    public Long getBloodPressureId() {
        return bloodPressureId;
    }

    public Long getUserId() {
        return userId;
    }

    public LocalDateTime getMeasuredAt() {
        return measuredAt;
    }

    public Integer getSystolic() {
        return systolic;
    }

    public Integer getDiastolic() {
        return diastolic;
    }

    public Integer getPulse() {
        return pulse;
    }

    public String getMemo() {
        return memo;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}