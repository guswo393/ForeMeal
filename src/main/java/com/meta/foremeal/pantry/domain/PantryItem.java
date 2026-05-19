package com.meta.foremeal.pantry.domain;

import com.meta.foremeal.foodmaster.domain.FoodMasterEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "user_pantry")
@Getter
@NoArgsConstructor
public class PantryItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "pantry_item_id")
    private Long itemId;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "food_id")
    private FoodMasterEntity foodMaster;

    @Column(name = "display_name", nullable = false, length = 100)
    private String displayName;

    @Column(name = "quantity")
    private Double quantity;

    @Column(name = "quantity_unit", length = 50)
    private String unit;

    @Column(name = "expiration_date")
    private LocalDate expirationDate;

    @Column(name = "inventory_state", length = 50)
    private String storageType;

    @Column(name = "memo", columnDefinition = "TEXT")
    private String memo;

    @Column(name = "is_staple")
    private Boolean isStaple;

    @Column(name = "added_at")
    private LocalDateTime addedAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "confidence_score")
    private Double confidenceScore;

    @Column(name = "entry_type", length = 50)
    private String entryType;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "scan_id")
    private PantryScan pantryScan;

    @Column(name = "custom_name")
    private String customName;

    @Column(name = "custom_calories_per_100g")
    private Double customCaloriesPer100g;

    @Column(name = "custom_sugar_per_100g")
    private Double customSugarPer100g;

    @Column(name = "custom_sodium_per_100g")
    private Double customSodiumPer100g;

    @Column(name = "custom_carbs_per_100g")
    private Double customCarbsPer100g;

    @Column(name = "custom_gi_index")
    private Double customGiIndex;

    @Builder
    public PantryItem(Long userId, FoodMasterEntity foodMaster, String displayName, Double quantity, String unit,
                      LocalDate expirationDate, String storageType, String memo, Boolean isStaple,
                      LocalDateTime addedAt, LocalDateTime updatedAt, Double confidenceScore, String entryType,
                      PantryScan pantryScan, String customName, Double customCaloriesPer100g,
                      Double customSugarPer100g, Double customSodiumPer100g, Double customCarbsPer100g,
                      Double customGiIndex) {
        this.userId = userId;
        this.foodMaster = foodMaster;
        this.displayName = displayName;
        this.quantity = quantity;
        this.unit = unit;
        this.expirationDate = expirationDate;
        this.storageType = storageType;
        this.memo = memo;
        this.isStaple = isStaple;
        this.addedAt = addedAt;
        this.updatedAt = updatedAt;
        this.confidenceScore = confidenceScore;
        this.entryType = entryType;
        this.pantryScan = pantryScan;
        this.customName = customName;
        this.customCaloriesPer100g = customCaloriesPer100g;
        this.customSugarPer100g = customSugarPer100g;
        this.customSodiumPer100g = customSodiumPer100g;
        this.customCarbsPer100g = customCarbsPer100g;
        this.customGiIndex = customGiIndex;
    }

    @PrePersist
    void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        if (addedAt == null) {
            addedAt = now;
        }
        if (updatedAt == null) {
            updatedAt = now;
        }
        if (isStaple == null) {
            isStaple = false;
        }
        if (entryType == null) {
            entryType = pantryScan == null ? "MANUAL" : "AI_SCAN";
        }
    }

    @PreUpdate
    void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
