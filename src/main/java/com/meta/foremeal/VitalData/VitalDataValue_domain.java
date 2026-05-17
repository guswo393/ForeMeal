package com.meta.foremeal.VitalData;

import com.meta.foremeal.user.domain.User;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import jakarta.persistence.Id;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "VitalData_value")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class VitalDataValue_domain {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "value_id")
    private Long valueId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    private LocalDate date; // 측정 날짜

    // 혈당 관련 (Glucose)
    @Column(name = "glucose_min")
    private Double glucoseMin;

    @Column(name = "glucose_avg")
    private Double glucoseAvg;

    @Column(name = "glucose_max")
    private Double glucoseMax;

    // 혈압 관련 (Blood Pressure)
    @Column(name = "bp_sys_min")
    private Double bpSysMin;

    @Column(name = "bp_sys_avg")
    private Double bpSysAvg;

    @Column(name = "bp_dia_avg")
    private Double bpDiaAvg;

    @Column(name = "measure_count")
    private Integer measureCount; // 당일 측정 횟수

    @Column(name = "computed_at")
    private LocalDateTime computedAt; // 통계 계산 시점
}