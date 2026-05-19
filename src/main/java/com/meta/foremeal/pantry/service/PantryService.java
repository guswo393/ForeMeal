package com.meta.foremeal.pantry.service;

import com.meta.foremeal.foodmaster.domain.FoodMasterEntity;
import com.meta.foremeal.foodmaster.repo.FoodMasterRepository;
import com.meta.foremeal.pantry.domain.IngredientAlias;
import com.meta.foremeal.pantry.domain.PantryItem;
import com.meta.foremeal.pantry.domain.PantryScan;
import com.meta.foremeal.pantry.repository.IngredientAliasRepository;
import com.meta.foremeal.pantry.repository.PantryItemRepository;
import com.meta.foremeal.pantry.repository.PantryScanRepository;
import com.meta.foremeal.user.domain.User;
import com.meta.foremeal.user.repo.UserRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PantryService {

    private static final Logger log = LoggerFactory.getLogger(PantryService.class);

    private final PantryItemRepository pantryItemRepository;
    private final FoodMasterRepository foodMasterRepository;
    private final PantryScanRepository pantryScanRepository;
    private final IngredientAliasRepository ingredientAliasRepository;
    private final UserRepository userRepository;
    private final RestTemplate restTemplate = new RestTemplate();

    @Transactional
    public List<PantryScanItemResponse> scanImage(Long userId, String imageUrl) {
        if (userId == null) {
            throw new IllegalArgumentException("userId is required.");
        }
        if (!StringUtils.hasText(imageUrl)) {
            throw new IllegalArgumentException("imageUrl is required.");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found."));

        PantryScan pantryScan = PantryScan.builder()
                .user(user)
                .imageUrl(imageUrl)
                .status("PROCESSING")
                .createdAt(LocalDateTime.now())
                .scannedAt(LocalDateTime.now())
                .build();
        pantryScanRepository.save(pantryScan);

        Map<String, String> aiRequest = new HashMap<>();
        aiRequest.put("imageUrl", imageUrl);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Map<String, String>> entity = new HttpEntity<>(aiRequest, headers);

        String aiServerUrl = "http://localhost:8000/predict";

        try {
            ResponseEntity<PantryDetectionResponse> aiResponse = restTemplate.exchange(
                    aiServerUrl,
                    HttpMethod.POST,
                    entity,
                    PantryDetectionResponse.class
            );

            PantryDetectionResponse aiResults = aiResponse.getBody();

            if (aiResults == null || aiResults.getItems() == null) {
                pantryScan.updateScanResult("SUCCESS", null, null);
                return List.of();
            }

            pantryScan.updateScanResult("SUCCESS", null, null);

            return aiResults.getItems().stream()
                    .filter(result -> result != null && StringUtils.hasText(result.getName()))
                    .map(result -> toScanItemResponse(pantryScan.getScanId(), result))
                    .collect(Collectors.toList());
        } catch (Exception e) {
            pantryScan.updateScanResult("FAILED", null, e.getMessage());
            log.error("Pantry scan failed. userId={}, imageUrl={}", userId, imageUrl, e);
            throw new IllegalStateException("AI scan failed: " + e.getMessage(), e);
        }
    }

    private PantryScanItemResponse toScanItemResponse(Long scanId, DetectedPantryItem detectedItem) {
        String detectedName = detectedItem.getName();
        IngredientAlias alias = ingredientAliasRepository
                .findFirstByDetectedNameIgnoreCaseAndEnabledTrueOrderByAliasIdAsc(detectedName)
                .orElse(null);
        String searchKeyword = alias != null ? alias.getSearchKeyword() : detectedName;
        String displayName = alias != null ? alias.getDisplayName() : detectedName;

        FoodMasterEntity matchedFood = alias != null && alias.getFoodMaster() != null
                ? alias.getFoodMaster()
                : foodMasterRepository.findByFoodNameContainingIgnoreCase(searchKeyword)
                .stream()
                .findFirst()
                .orElse(null);

        if (matchedFood == null) {
            return new PantryScanItemResponse(
                    scanId,
                    detectedName,
                    displayName,
                    null,
                    detectedItem.getConfidence(),
                    false
            );
        }

        return new PantryScanItemResponse(
                scanId,
                detectedName,
                matchedFood.getFoodName(),
                matchedFood.getFoodId(),
                detectedItem.getConfidence(),
                true
        );
    }

    @Transactional
    public Long addItem(PantryItemRequest request) {
        PantryScan pantryScan = null;
        if (request.getScanId() != null) {
            pantryScan = pantryScanRepository.findById(request.getScanId())
                    .orElseThrow(() -> new IllegalArgumentException("Scan history not found."));
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
                .orElseThrow(() -> new IllegalArgumentException("Pantry item not found."));

        if (!pantryItem.getUserId().equals(userId)) {
            throw new IllegalStateException("No permission.");
        }

        pantryItemRepository.delete(pantryItem);
    }
}
