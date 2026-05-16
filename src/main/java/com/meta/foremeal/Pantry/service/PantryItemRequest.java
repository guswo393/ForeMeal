package com.meta.foremeal.Pantry.service;

import lombok.Getter;
import lombok.Setter;
import java.time.LocalDate;

@Getter
@Setter

public class PantryItemRequest {
    private Long userId;
    private String inputName;   // 사용자 직접 입력 혹은 AI가 인식한 이름
    private Double quantity;
    private String unit;
    private LocalDate expirationDate;
    private String storageType;
    private String memo;
    private Long scanId;

    // custom nutrition info (foodMaster에 없을 때만 채워짐)
    private Double calories;
    private Double sugar;
    private Double carbs;
    private Double sodium;
    private Double giIndex;
}


