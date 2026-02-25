package com.meta.foremeal.meallog.domain;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(
        name = "daily_intake_summary",
        uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "summary_date"})
)
public class DailyIntakeSummary {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "summary_id")
    private Long summaryId;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "summary_date", nullable = false)
    private LocalDate summaryDate;

    @Column(name = "total_calories", nullable = false, precision = 12, scale = 2)
    private BigDecimal totalCalories = BigDecimal.ZERO;

    @Column(name = "total_sodium", nullable = false, precision = 12, scale = 2)
    private BigDecimal totalSodium = BigDecimal.ZERO;

    @Column(name = "total_sugar", nullable = false, precision = 12, scale = 2)
    private BigDecimal totalSugar = BigDecimal.ZERO;

    @Column(name = "total_carbs", nullable = false, precision = 12, scale = 2)
    private BigDecimal totalCarbs = BigDecimal.ZERO;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt = LocalDateTime.now();

    protected DailyIntakeSummary() {}

    public DailyIntakeSummary(Long userId, LocalDate summaryDate) {
        this.userId = userId;
        this.summaryDate = summaryDate;
    }

    public void add(BigDecimal calories, BigDecimal sodium, BigDecimal sugar, BigDecimal carbs) {
        this.totalCalories = this.totalCalories.add(nz(calories));
        this.totalSodium = this.totalSodium.add(nz(sodium));
        this.totalSugar = this.totalSugar.add(nz(sugar));
        this.totalCarbs = this.totalCarbs.add(nz(carbs));
        this.updatedAt = LocalDateTime.now();
    }

    private BigDecimal nz(BigDecimal v) {
        return v == null ? BigDecimal.ZERO : v;
    }

    public Long getSummaryId() { return summaryId; }
    public Long getUserId() { return userId; }
    public LocalDate getSummaryDate() { return summaryDate; }
    public BigDecimal getTotalCalories() { return totalCalories; }
    public BigDecimal getTotalSodium() { return totalSodium; }
    public BigDecimal getTotalSugar() { return totalSugar; }
    public BigDecimal getTotalCarbs() { return totalCarbs; }
}
