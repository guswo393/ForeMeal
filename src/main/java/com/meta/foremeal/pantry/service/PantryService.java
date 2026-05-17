package com.meta.foremeal.pantry.service;

import com.meta.foremeal.pantry.domain.PantryItem;
import com.meta.foremeal.pantry.domain.PantryScan;
import com.meta.foremeal.pantry.repository.PantryItemRepository;
import com.meta.foremeal.pantry.repository.PantryScanRepository;
import com.meta.foremeal.foodmaster.domain.FoodMasterEntity;
import com.meta.foremeal.foodmaster.repo.FoodMasterRepository;
import com.meta.foremeal.user.domain.User;
import com.meta.foremeal.user.repo.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import org.springframework.web.client.RestTemplate;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly=true)

public class PantryService {

    private final PantryItemRepository pantryItemRepository;
    private final FoodMasterRepository foodMasterRepository;
    private final PantryScanRepository pantryScanRepository;
    private final UserRepository userRepository;
    // 외부 ai 서버와 통신하기 위한 객체
    private final RestTemplate restTemplate = new RestTemplate();


    // 1. 냉장고 이미지 스캔 및 AI 서버 분석 결과 반환
    @Transactional
    public List<PantryItemResponse> scanImage(Long userId, String imageUrl) {
       // 1. 유저 조회 및 스캔 이력 저장
        User user = userRepository.findById(userId)
                .orElseThrow(()->new IllegalArgumentException("존재하지 않는 유저입니다."));

        PantryScan pantryScan = PantryScan.builder()
                .user(user)
                .imageUrl(imageUrl)
                .status("PROCESSING")
                .createdAt(LocalDateTime.now())
                .scannedDate(LocalDateTime.now())
                .build();
        pantryScanRepository.save(pantryScan);

        // 2. 실제 AI 모델 서버 호출
        // AI 서버로 보낼 요청 데이터(Request Body) 설정
        Map<String, String> aiRequest = new HashMap<>();
        aiRequest.put("imageUrl", imageUrl);

        HttpHeaders headers=new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Map<String, String>> entity=new HttpEntity<>(aiRequest, headers);

        // AI 서버 URL (실제 파이썬 서버 주소나 API 주소로 변경)
        String aiServerUrl="http://localhost:8000/predict";

        List<PantryItemResponse> responses=new ArrayList<>();

        try{
            //AI 서버에 POST 요청을 보내고, 실제 응답을 PantryItemRequest 구조의 리스트로 받아옴
            // (PantryItemRequest가 name과 quantity를 가지고 있으므로 바구니로 재사용 가능)
            ResponseEntity<List<PantryItemRequest>> aiResponse = restTemplate.exchange(
                    aiServerUrl,
                    HttpMethod.POST,
                    entity,
                    new ParameterizedTypeReference<List<PantryItemRequest>>() {}
            );

            List<PantryItemRequest> aiResults = aiResponse.getBody();

            // 3. AI 실시간 분석 결과를 PantryItemResponse로 변환
            if (aiResults != null) {
                responses = aiResults.stream()
                        .map(result -> PantryItemResponse.builder()
                                .scanId(pantryScan.getScanId())
                                .displayName(result.getInputName()) // 💡 실제 필드명 displayName에 매핑!
                                .quantity(result.getQuantity() != null ? result.getQuantity().doubleValue() : 1.0) // Double 타입 싱크 맞춤
                                .expirationDate(null) // 유통기한은 유저가 검토화면에서 채워 넣음
                                .build())
                        .collect(Collectors.toList());

                // 스캔 이력 내역 업데이트
                pantryScan.updateScanResult("PROCESSING", aiResults.toString(), null);
            }

        } catch (Exception e) {
            // AI 서버가 죽었거나 통신 장애가 발생했을 때 예외 처리 (Extreme Situation 방어)
            pantryScan.updateScanResult("FAILED", null, e.getMessage());
            throw new IllegalStateException("AI 분석 서버와의 통신에 실패했습니다: " + e.getMessage());
        }

        return responses;
    }


    // 2. 냉장고에 재료 추가 (수동 입력 및 AI 스캔 공통 사용)
    @Transactional
    public Long addItem(PantryItemRequest request){
        // AI 스캔 이력 조회 (있으면 연결)
        PantryScan pantryScan=null;
        if(request.getScanId()!=null){
            pantryScan=pantryScanRepository.findById(request.getScanId())
                    .orElseThrow(()->new IllegalArgumentException("존재하지 않는 스캔 이력입니다."));
        }

        // 이름 기반으로 마스터 DB에 등록된 식품이 있는지 확인
        FoodMasterEntity foodMaster = foodMasterRepository.findByFoodNameContaining(request.getInputName())
                .stream()
                .filter(food -> food.getFoodName().equalsIgnoreCase(request.getInputName()))
                .findFirst()
                .orElse(null);

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

    // 3. 특정 사용자의 전체 냉장고 재료 목록 조회
    public List<PantryItemResponse> getMyPantry(Long userId){
        // N+1 방지를 위해 Fetch Join 쿼리 사용
        List<PantryItem> pantryItem=pantryItemRepository.findAllByUserIdWithFoodMaster(userId);

        return pantryItem.stream()
                .map(PantryItemResponse::new)
                .collect(Collectors.toList());
    }

    // 4. 냉장고 재료 삭제
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
