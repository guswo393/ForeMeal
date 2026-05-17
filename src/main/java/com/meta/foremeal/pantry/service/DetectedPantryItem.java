package com.meta.foremeal.pantry.service;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DetectedPantryItem {
    private String name;
    private Double quantity;
    private Double confidence;
}
