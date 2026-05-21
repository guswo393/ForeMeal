package com.meta.foremeal.pantry.controller;

import com.meta.foremeal.pantry.service.PantryItemRequest;
import com.meta.foremeal.pantry.service.PantryItemResponse;
import com.meta.foremeal.pantry.service.PantryScanConfirmRequest;
import com.meta.foremeal.pantry.service.PantryScanConfirmResponse;
import com.meta.foremeal.pantry.service.PantryScanItemResponse;
import com.meta.foremeal.pantry.service.PantryScanRequest;
import com.meta.foremeal.pantry.service.PantryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/pantry")
@RequiredArgsConstructor
public class PantryController {
    private final PantryService pantryService;

    @PostMapping("/items")
    public ResponseEntity<Long> addItem(@RequestBody PantryItemRequest request) {
        Long savedPantryId = pantryService.addItem(request);
        return ResponseEntity.ok(savedPantryId);
    }

    @GetMapping("/items")
    public ResponseEntity<List<PantryItemResponse>> getMyPantry(@RequestParam("userId") Long userId) {
        List<PantryItemResponse> myPantry = pantryService.getMyPantry(userId);
        return ResponseEntity.ok(myPantry);
    }

    @DeleteMapping("/items/{itemId}")
    public ResponseEntity<Void> deleteItem(
            @PathVariable("itemId") Long itemId,
            @RequestParam("userId") Long userId) {
        pantryService.deleteItem(itemId, userId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/scans")
    public ResponseEntity<List<PantryScanItemResponse>> requestScan(@RequestBody PantryScanRequest request) {
        List<PantryScanItemResponse> detectedItems = pantryService.scanImage(request.getUserId(), request.getImageUrl());
        return ResponseEntity.ok(detectedItems);
    }

    @PostMapping("/scans/{scanId}/confirm")
    public ResponseEntity<PantryScanConfirmResponse> confirmScan(
            @PathVariable("scanId") Long scanId,
            @RequestBody PantryScanConfirmRequest request) {
        PantryScanConfirmResponse response = pantryService.confirmScan(scanId, request);
        return ResponseEntity.ok(response);
    }
}
