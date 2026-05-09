package com.meta.foremeal.meallog.domain;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "meal_log_item")
public class MealLogItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "item_id")
    private Long itemId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "meal_id", nullable = false)
    private MealLog mealLog;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "food_id")
    private Long foodId;

    @Column(name = "food_name", nullable = false, length = 255)
    private String foodName;

    @Column(name = "quantity", nullable = false, precision = 10, scale = 2)
    private BigDecimal quantity;

    @Column(name = "unit", nullable = false, length = 30)
    private String unit;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    protected MealLogItem() {}

    public MealLogItem(Long userId, Long foodId, String foodName, BigDecimal quantity, String unit) {
        this.userId = userId;
        this.foodId = foodId;
        this.foodName = foodName;
        this.quantity = quantity;
        this.unit = unit;
    }

    void attach(MealLog mealLog) {
        this.mealLog = mealLog;
    }

    public Long getItemId() { return itemId; }
    public Long getUserId() { return userId; }
    public Long getFoodId() { return foodId; }
    public String getFoodName() { return foodName; }
    public BigDecimal getQuantity() { return quantity; }
    public String getUnit() { return unit; }
}