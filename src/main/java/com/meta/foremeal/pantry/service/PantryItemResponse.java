package com.meta.foremeal.pantry.service;

import com.meta.foremeal.pantry.domain.PantryItem;
import lombok.Getter;
import java.time.LocalDate;

@Getter

public class PantryItemResponse {
    private Long itemId;
    private String displayName;
    private Double quantity;
    private String unit;
    private String storageType;
    private LocalDate expirationDate;
    private String memo;
    private boolean isCustom;   // 커스텀 재료 여부

    // 통합 영양 성분
    private Double calories;
    private Double sugar;
    private Double carbs;
    private Double sodium;
    private Double giIndex;

    public PantryItemResponse(PantryItem pantryItem){
        this.itemId=pantryItem.getItemId();
        this.displayName=pantryItem.getDisplayName();
        this.quantity=pantryItem.getQuantity();
        this.unit=pantryItem.getUnit();
        this.storageType=pantryItem.getStorageType();
        this.expirationDate=pantryItem.getExpirationDate();
        this.memo=pantryItem.getMemo();

        // 핵심 로직 : 마스터 DB 존재 여부에 따라 영양소 데이터를 다르게 매핑
        if(pantryItem.getFoodMaster()!=null) {
            this.isCustom=false;
            this.calories=pantryItem.getFoodMaster().getCalories();
            this.sugar=pantryItem.getFoodMaster().getSugar();
            this.carbs=pantryItem.getFoodMaster().getCarbs();
            this.sodium=pantryItem.getFoodMaster().getSodium();
            this.giIndex=pantryItem.getFoodMaster().getGiIndex();
        }else{
            this.isCustom=true;
            this.calories=pantryItem.getCustomCaloriesPer100g();
            this.sugar=pantryItem.getCustomSugarPer100g();
            this.carbs=pantryItem.getCustomCarbsPer100g();
            this.sodium=pantryItem.getCustomSodiumPer100g();
            this.giIndex=pantryItem.getCustomGiIndex();
        }
    }
}
