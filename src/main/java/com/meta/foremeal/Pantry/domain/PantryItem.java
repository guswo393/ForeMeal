package com.meta.foremeal.Pantry.domain;

import com.meta.foremeal.FoodMaster.FoodMasterEntity;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import java.time.LocalDate;

@Entity
@Table(name = "PantryItem")
@Getter
@NoArgsConstructor
public class PantryItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "item_id") // 보유 재료 자체의 PK
    private Long itemId;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "food_id", nullable = true)
    private FoodMasterEntity foodMaster;

    @Column(name = "display_name", nullable = false)
    private String displayName; // 화면에 보여줄 재료/제품명

    private Double quantity; // 수량
    private String unit;     // 단위 (g, 개, 팩 등)

    @Column(name = "expiration_date")
    private LocalDate expirationDate; // 유통기한

    @Column(name = "storage_type")
    private String storageType; // 보관 방식 (냉장, 냉동, 실온 등)

    @Column(columnDefinition = "TEXT")
    private String memo; // 메모

    // 사진 스캔 이력 연동
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "scan_id", nullable=true)
    private PantryScan pantryScan;

    // 마스터 DB에 없을 때 사용하는 커스텀 스냅샷 (Nutrition Snapshot) 필드군
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
                      LocalDate expirationDate, String storageType, String memo, PantryScan pantryScan,
                      String customName, Double customCaloriesPer100g, Double customSugarPer100g,
                      Double customSodiumPer100g, Double customCarbsPer100g, Double customGiIndex) {
        this.userId = userId;
        this.foodMaster = foodMaster;
        this.displayName = displayName;
        this.quantity = quantity;
        this.unit = unit;
        this.expirationDate = expirationDate;
        this.storageType = storageType;
        this.memo = memo;
        this.pantryScan = pantryScan;
        this.customName = customName;
        this.customCaloriesPer100g = customCaloriesPer100g;
        this.customSugarPer100g = customSugarPer100g;
        this.customSodiumPer100g = customSodiumPer100g;
        this.customCarbsPer100g = customCarbsPer100g;
        this.customGiIndex = customGiIndex;
    }
}