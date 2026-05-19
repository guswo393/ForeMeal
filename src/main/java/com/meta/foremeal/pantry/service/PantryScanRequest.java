package com.meta.foremeal.pantry.service;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PantryScanRequest {
    private Long userId;
    private String imageUrl;
}
