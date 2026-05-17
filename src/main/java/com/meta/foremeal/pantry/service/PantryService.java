package com.meta.foremeal.pantry.service;

import com.meta.foremeal.foodmaster.domain.FoodMasterEntity;
import com.meta.foremeal.foodmaster.repo.FoodMasterRepository;
import com.meta.foremeal.pantry.domain.PantryItem;
import com.meta.foremeal.pantry.domain.PantryScan;
import com.meta.foremeal.pantry.repository.PantryItemRepository;
import com.meta.foremeal.pantry.repository.PantryScanRepository;
import com.meta.foremeal.user.domain.User;
import com.meta.foremeal.user.repo.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PantryService {

    private final PantryItemRepository pantryItemRepository;
    private final FoodMasterRepository foodMasterRepository;
    private final PantryScanRepository pantryScanRepository;
    private final UserRepository userRepository;
    private final RestTemplate restTemplate = new RestTemplate();

    @Transactional
    public List<PantryItemResponse> scanImage(Long userId, String imageUrl) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 유저입니다."));

        PantryScan pantryScan = PantryScan.builder()
                .user(user)
                .imageUrl(imageUrl)
                .status("PROCESSING")
                .createdAt(LocalDateTime.now())
                .scannedDate(LocalDateTime.now())
                .build();
        pantryScanRepository.save(pantryScan);

        Map<String, String> aiRequest = new HashMap<>();
        aiRequest.put("imageUrl", imageUrl);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Map<String, String>> entity = new HttpEntity<>(aiRequest, headers);

        String aiServerUrl = "http://localhost:8000/predict";
        List<PantryItemResponse> responses = new ArrayList<>();

        try {
            ResponseEntity<PantryDetectionResponse> aiResponse = restTemplate.exchange(
                    aiServerUrl,
                    HttpMethod.POST,
                    entity,
                    PantryDetectionResponse.class
            );

            PantryDetectionResponse aiResults = aiResponse.getBody();

            if (aiResults != null && aiResults.getItems() != null) {
                responses = aiResults.getItems().stream()
                        .map(result -> PantryItemResponse.builder()
                                .scanId(pantryScan.getScanId())
                                .displayName(result.getName())
                                .quantity(result.getQuantity() != null ? result.getQuantity() : 1.0)
                                .confidence(result.getConfidence())
                                .expirationDate(null)
                                .build())
                        .collect(Collectors.toList());

                pantryScan.updateScanResult("SUCCESS", null, null);
            }
        } catch (Exception e) {
            pantryScan.updateScanResult("FAILED", null, e.getMessage());
            throw new IllegalStateException("AI 분석 서버와의 통신에 실패했습니다: " + e.getMessage());
        }

        return responses;
    }

    @Transactional
    public Long addItem(PantryItemRequest request) {
        PantryScan pantryScan = null;
        if (request.getScanId() != null) {
            pantryScan = pantryScanRepository.findById(request.getScanId())
                    .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 스캔 이력입니다."));
        }

        FoodMasterEntity foodMaster = foodMasterRepository.findByFoodNameContaining(request.getInputName())
                .stream()
                .filter(food -> food.getFoodName().equalsIgnoreCase(request.getInputName()))
                .findFirst()
                .orElse(null);

        PantryItem.PantryItemBuilder builder = PantryItem.builder()
                .userId(request.getUserId())
                .displayName(request.getInputName())
                .quantity(request.getQuantity())
                .unit(request.getUnit())
                .expirationDate(request.getExpirationDate())
                .storageType(request.getStorageType())
                .memo(request.getMemo())
                .pantryScan(pantryScan);

        if (foodMaster != null) {
            builder.foodMaster(foodMaster);
        } else {
            builder.foodMaster(null)
                    .customName(request.getInputName())
                    .customCaloriesPer100g(request.getCalories())
                    .customSugarPer100g(request.getSugar())
                    .customCarbsPer100g(request.getCarbs())
                    .customSodiumPer100g(request.getSodium())
                    .customGiIndex(request.getGiIndex());
        }

        PantryItem savedPantryItem = pantryItemRepository.save(builder.build());
        return savedPantryItem.getItemId();
    }

    public List<PantryItemResponse> getMyPantry(Long userId) {
        List<PantryItem> pantryItems = pantryItemRepository.findAllByUserIdWithFoodMaster(userId);

        return pantryItems.stream()
                .map(PantryItemResponse::new)
                .collect(Collectors.toList());
    }

    @Transactional
    public void deleteItem(Long itemId, Long userId) {
        PantryItem pantryItem = pantryItemRepository.findById(itemId)
                .orElseThrow(() -> new IllegalArgumentException("해당 재료가 냉장고에 존재하지 않습니다."));

        if (!pantryItem.getUserId().equals(userId)) {
            throw new IllegalStateException("권한이 없습니다.");
        }

        pantryItemRepository.delete(pantryItem);
    }
}
