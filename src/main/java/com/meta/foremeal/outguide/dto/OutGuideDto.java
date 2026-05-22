package com.meta.foremeal.outguide.dto;

import java.math.BigDecimal;
import java.util.List;

public class OutGuideDto {

    public enum IntakeStatus {
        LOW,
        NORMAL,
        HIGH
    }

    public enum RiskLevel {
        LOW,
        CAUTION,
        HIGH
    }

    public record DailyContext(
            BigDecimal totalCalories,
            BigDecimal totalCarbs,
            BigDecimal totalSodium,
            BigDecimal totalSugar,
            IntakeStatus caloriesStatus,
            IntakeStatus carbsStatus,
            IntakeStatus sodiumStatus,
            IntakeStatus sugarStatus
    ) {}

    public record RestaurantResponse(
            String placeId,
            String name,
            String category,
            String categoryDetail,
            String address,
            Integer distanceMeters,
            BigDecimal lat,
            BigDecimal lng,
            RiskLevel riskLevel,
            List<String> riskTags,
            String riskMessage
    ) {}

    public record RestaurantSearchResponse(
            String date,
            DailyContext dailyContext,
            List<RestaurantResponse> restaurants
    ) {}

    public record LocationResponse(
            String query,
            String address,
            BigDecimal lat,
            BigDecimal lng
    ) {}

    public record RestaurantCandidate(
            String placeId,
            String name,
            String categoryDetail,
            String address,
            Integer distanceMeters,
            BigDecimal lat,
            BigDecimal lng
    ) {}

    public record RiskResult(
            String category,
            RiskLevel riskLevel,
            List<String> riskTags,
            String riskMessage
    ) {}
}
