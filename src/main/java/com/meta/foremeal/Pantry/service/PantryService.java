package com.meta.foremeal.Pantry.service;

import com.meta.foremeal.Pantry.domain.PantryItem;
import com.meta.foremeal.Pantry.domain.PantryScan;
import com.meta.foremeal.FoodMaster.FoodMasterEntity;
import com.meta.foremeal.Pantry.service.PantryItemRequest;
import com.meta.foremeal.Pantry.service.PantryItemResponse;
import com.meta.foremeal.Pantry.repository.PantryItemRepository;
import com.meta.foremeal.Pantry.repository.PantryScanRepository;
import com.meta.foremeal.FoodMaster.FoodMasterRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly=true)

public class PantryService {

    private final PantryItemRepository pantryItemRepository;
    private final FoodMasterRepository foodMasterRepository;
    private final PantryScanRepository pantryScanRepository;


    // 1. 냉장고에 재료 추가 (수동 입력 및 AI 스캔 공통 사용)
    @Transactional
    public Long addItem(PantryItemRequest request){
        // AI 스캔 이력 조회 (있으면 연결)
        PantryScan pantryScan=null;
        if(request.getScanId()!=null){
            pantryScan=pantryScanRepository.findById(request.getScanId())
                    .orElseThrow(()->new IllegalArgumentException("존재하지 않는 스캔 이력입니다."));
        }

        // 이름 기반으로 마스터 DB에 등록된 식품이 있는지 확인
        FoodMasterEntity foodMaster = foodMasterRepository.findByFoodName(request.getInputName()).orElse(null);

        PantryItem.PantryItemBuilder builder=PantryItem.builder()
                .userId(request.getUserId())
                .displayName(request.getInputName())
                .quantity(request.getQuantity())
                .unit(request.getUnit())
                .expirationDate(request.getExpirationDate())
                .storageType(request.getStorageType())
                .memo(request.getMemo())
                .pantryScan(pantryScan);

        if(foodMaster !=null){
            // CASE 1 : 마스터 DB에 일치하는 식품이 있는 경우 => 외래키 매핑만 진행
            builder.foodMaster(foodMaster);
        }else{
            // CASE 2 : 마스터 DB에 없는 경우 => 사용자가 넘겨준 영양성분을 스냅샷 필드에 저장
            builder.foodMaster(null)
                    .customName(request.getInputName())
                    .customCaloriesPer100g(request.getCalories())
                    .customSugarPer100g(request.getSugar())
                    .customCarbsPer100g(request.getCarbs())
                    .customSodiumPer100g(request.getSodium())
                    .customGiIndex(request.getGiIndex());
        }

        PantryItem savedPantryItem=pantryItemRepository.save(builder.build());
        return savedPantryItem.getItemId();
    }

    // 2. 특정 사용자의 전체 냉장고 재료 목록 조회
    public List<PantryItemResponse> getMyPantry(Long userId){
        // N+1 방지를 위해 Fetch Join 쿼리 사용
        List<PantryItem> pantryItem=pantryItemRepository.findAllByUserIdWithFoodMaster(userId);

        return pantryItem.stream()
                .map(PantryItemResponse::new)
                .collect(Collectors.toList());
    }

    // 3. 냉장고 재료 삭제
    @Transactional
    public void deleteItem(Long itemId, Long userId){
        PantryItem pantryItem=pantryItemRepository.findById(itemId)
                .orElseThrow(()->new IllegalArgumentException("해당 재료가 냉장고에 존재하지 않습니다."));

        // 본인 냉장고 재료가 맞는지 검증 구문 추가
        if(!pantryItem.getUserId().equals(userId)){
            throw new IllegalStateException("권한이 없습니다.");
        }

        pantryItemRepository.delete(pantryItem);
    }
}
