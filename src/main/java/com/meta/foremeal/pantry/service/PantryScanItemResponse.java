package com.meta.foremeal.pantry.service;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class PantryScanItemResponse {
    private Long scanId;
    private String detectedName;
    private String displayName;
    private Long foodId;
    private Double confidence;
    private boolean matched;
}
