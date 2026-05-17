package com.meta.foremeal.pantry.service;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class PantryScanItemResponse {
    private Long scanId;
    private String name;
    private Double confidence;
}
