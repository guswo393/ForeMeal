package com.meta.foremeal.pantry.service;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
public class PantryScanConfirmRequest {
    private Long userId;
    private List<Item> items;

    @Getter
    @Setter
    public static class Item {
        private Boolean selected;
        private String detectedName;
        private String inputName;
        private Long foodId;
        private Double confidence;
        private Double quantity;
        private String unit;
        private LocalDate expirationDate;
        private String storageType;
        private String memo;
        private Double calories;
        private Double sugar;
        private Double carbs;
        private Double sodium;
        private Double giIndex;
    }
}
