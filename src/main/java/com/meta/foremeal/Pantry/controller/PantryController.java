package com.meta.foremeal.Pantry.controller;

import com.meta.foremeal.Pantry.service.PantryItemRequest;
import com.meta.foremeal.Pantry.service.PantryItemResponse;
import com.meta.foremeal.Pantry.service.PantryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/pantry")
@RequiredArgsConstructor

public class PantryController {
    private final PantryService pantryService;
    // private final PantryScanService pantryScanService; // 추후 AI 이미지 인식 기능 구현 시 추가

    // 1. 냉장고 재료 관련 API
    // 냉장고 재료 추가
    @PostMapping("/items")
    public ResponseEntity<Long> addItem(@RequestBody PantryItemRequest request) {
        Long savedPantryId = pantryService.addItem(request);
        return ResponseEntity.ok(savedPantryId);
    }

    // 특정 사용자의 전체 냉장고 재료 목록 조회
    @GetMapping("/items")
    public ResponseEntity<List<PantryItemResponse>> getMyPantry(@RequestParam("userId") Long userId){
        List<PantryItemResponse> myPantry = pantryService.getMyPantry(userId);
        return ResponseEntity.ok(myPantry);
    }

    // 냉장고 재료 삭제
    @DeleteMapping("/items/{itemId}")
    public ResponseEntity<Void> deleteItem(
            @PathVariable("itemId") Long itemId,
            @RequestParam("userId") Long userId){
        pantryService.deleteItem(itemId, userId);
        return ResponseEntity.noContent().build();
    }


    // 2. 냉장고 사진 스캔 관련 API (추후 확장 시 추가)
    /*
    // 냉장고 사진 스캔 요청 (AI 분석 시작)
    @PostMapping("/scans")
    public ResponseEntity<Long> requestScan(@RequestBody PantryScanRequest request){
        // AI 엔진에 사진 던지고 스캔 이력(scanId) 파싱하는 로직 호출
        Long scanId=pantryScanService.createScan(request);
        return ResponseEntity.ok(scanId);
    }

    // 특정 유저의 과거 냉장고 사진 스캔 히스토리 전체 조회
    @GetMapping("/scans")
    public ResponseEntity<List<PantryScanResponse>> getMyScanHistory(@RequestParam("userId") Long userId){
        List<PantryScanResponse> history=pantryScanService.getHistory(userId);
        return ResponseEntity.ok(history);
        return null;
     }
     */
}
