package com.meta.foremeal.outguide.service;

import com.meta.foremeal.meallog.domain.DailyIntakeSummary;
import com.meta.foremeal.meallog.repo.DailyIntakeSummaryRepository;
import com.meta.foremeal.outguide.dto.OutGuideDto;
import com.meta.foremeal.outguide.external.KakaoLocalClient;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Service
public class OutGuideService {

    private static final int DEFAULT_RADIUS_METERS = 1_000;
    private static final int DEFAULT_SIZE = 45;

    private final KakaoLocalClient kakaoLocalClient;
    private final DailyIntakeSummaryRepository dailyIntakeSummaryRepository;
    private final RestaurantRiskService restaurantRiskService;

    public OutGuideService(KakaoLocalClient kakaoLocalClient,
                           DailyIntakeSummaryRepository dailyIntakeSummaryRepository,
                           RestaurantRiskService restaurantRiskService) {
        this.kakaoLocalClient = kakaoLocalClient;
        this.dailyIntakeSummaryRepository = dailyIntakeSummaryRepository;
        this.restaurantRiskService = restaurantRiskService;
    }

    @Transactional(readOnly = true)
    public OutGuideDto.RestaurantSearchResponse searchRestaurants(
            Long userId,
            BigDecimal lat,
            BigDecimal lng,
            String query,
            Integer radius,
            Integer size,
            LocalDate date
    ) {
        LocalDate targetDate = date == null ? LocalDate.now() : date;
        OutGuideDto.DailyContext dailyContext = getDailyContext(userId, targetDate);

        if (!StringUtils.hasText(query) && (lat == null || lng == null)) {
            throw new IllegalArgumentException("위치 기반 검색에는 위도와 경도가 필요합니다.");
        }

        List<OutGuideDto.RestaurantCandidate> candidates = StringUtils.hasText(query)
                ? kakaoLocalClient.searchRestaurantsByKeyword(query.trim(), lat, lng, radius, sizeOrDefault(size))
                : kakaoLocalClient.searchRestaurants(lat, lng, radiusOrDefault(radius), sizeOrDefault(size));

        List<OutGuideDto.RestaurantResponse> restaurants = candidates
                .stream()
                .map(candidate -> toResponse(candidate, dailyContext))
                .toList();

        return new OutGuideDto.RestaurantSearchResponse(
                targetDate.toString(),
                dailyContext,
                restaurants
        );
    }

    public OutGuideDto.LocationResponse searchLocation(String query) {
        if (!StringUtils.hasText(query)) {
            throw new IllegalArgumentException("주소를 입력해주세요.");
        }

        return kakaoLocalClient.searchLocation(query.trim());
    }

    private OutGuideDto.DailyContext getDailyContext(Long userId, LocalDate date) {
        DailyIntakeSummary summary = dailyIntakeSummaryRepository
                .findByUserIdAndSummaryDate(userId, date)
                .orElseGet(() -> new DailyIntakeSummary(userId, date));

        return restaurantRiskService.buildDailyContext(
                summary.getTotalCalories(),
                summary.getTotalCarbs(),
                summary.getTotalSodium(),
                summary.getTotalSugar()
        );
    }

    private OutGuideDto.RestaurantResponse toResponse(
            OutGuideDto.RestaurantCandidate candidate,
            OutGuideDto.DailyContext dailyContext
    ) {
        OutGuideDto.RiskResult risk = restaurantRiskService.evaluate(candidate.categoryDetail(), dailyContext);

        return new OutGuideDto.RestaurantResponse(
                candidate.placeId(),
                candidate.name(),
                risk.category(),
                candidate.categoryDetail(),
                candidate.address(),
                candidate.distanceMeters(),
                candidate.lat(),
                candidate.lng(),
                risk.riskLevel(),
                risk.riskTags(),
                risk.riskMessage()
        );
    }

    private int radiusOrDefault(Integer radius) {
        return radius == null ? DEFAULT_RADIUS_METERS : radius;
    }

    private int sizeOrDefault(Integer size) {
        return size == null ? DEFAULT_SIZE : size;
    }
}
