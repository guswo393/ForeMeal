package com.meta.foremeal.pantry.service;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class PantryScanConfirmResponse {
    private Long scanId;
    private int savedCount;
    private List<SavedItem> items;

    @Getter
    @AllArgsConstructor
    public static class SavedItem {
        private Long itemId;
        private String displayName;
    }
}
