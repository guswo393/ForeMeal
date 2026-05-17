package com.meta.foremeal.pantry.service;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class PantryDetectionResponse {
    private List<DetectedPantryItem> items;
}
