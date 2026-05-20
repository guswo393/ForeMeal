package com.meta.foremeal.outguide.service;

import com.meta.foremeal.outguide.dto.OutGuideDto;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Service
public class RestaurantRiskService {

    private static final BigDecimal CALORIES_HIGH = BigDecimal.valueOf(1600);
    private static final BigDecimal CARBS_HIGH = BigDecimal.valueOf(180);
    private static final BigDecimal SODIUM_HIGH = BigDecimal.valueOf(2000);
    private static final BigDecimal SUGAR_HIGH = BigDecimal.valueOf(50);

    public OutGuideDto.DailyContext buildDailyContext(BigDecimal calories, BigDecimal carbs, BigDecimal sodium, BigDecimal sugar) {
        return new OutGuideDto.DailyContext(
                nz(calories),
                nz(carbs),
                nz(sodium),
                nz(sugar),
                statusOf(calories, CALORIES_HIGH),
                statusOf(carbs, CARBS_HIGH),
                statusOf(sodium, SODIUM_HIGH),
                statusOf(sugar, SUGAR_HIGH)
        );
    }

    public OutGuideDto.RiskResult evaluate(String categoryDetail, OutGuideDto.DailyContext dailyContext) {
        String category = normalizeCategory(categoryDetail);
        List<String> tags = new ArrayList<>();
        OutGuideDto.RiskLevel level = OutGuideDto.RiskLevel.LOW;
        String message;

        switch (category) {
            case "분식" -> {
                tags.add("탄수화물");
                tags.add("나트륨");
                level = elevatedLevel(dailyContext.carbsStatus(), dailyContext.sodiumStatus());
                message = dailyContext.carbsStatus() == OutGuideDto.IntakeStatus.HIGH
                        ? "오늘 탄수화물 섭취량이 높은 편이라 분식 메뉴는 탄수화물과 나트륨이 몰릴 수 있어요."
                        : "분식은 면, 밥, 떡 조합이 많아 탄수화물과 나트륨이 함께 높아질 수 있어요.";
            }
            case "중식" -> {
                tags.add("나트륨");
                tags.add("탄수화물");
                level = elevatedLevel(dailyContext.sodiumStatus(), dailyContext.carbsStatus());
                message = dailyContext.sodiumStatus() == OutGuideDto.IntakeStatus.HIGH
                        ? "오늘 나트륨 섭취량이 높은 편이라 중식은 국물과 소스 양을 조절하는 게 좋아요."
                        : "중식은 면, 볶음밥, 소스류 선택에 따라 탄수화물과 나트륨이 빠르게 올라갈 수 있어요.";
            }
            case "패스트푸드" -> {
                tags.add("나트륨");
                tags.add("지방");
                level = dailyContext.sodiumStatus() == OutGuideDto.IntakeStatus.HIGH
                        ? OutGuideDto.RiskLevel.HIGH
                        : OutGuideDto.RiskLevel.CAUTION;
                message = "패스트푸드는 세트 구성에서 나트륨과 지방이 높아지기 쉬워 단품이나 사이드 조절이 좋아요.";
            }
            case "카페" -> {
                tags.add("당류");
                level = dailyContext.sugarStatus() == OutGuideDto.IntakeStatus.HIGH
                        ? OutGuideDto.RiskLevel.HIGH
                        : OutGuideDto.RiskLevel.CAUTION;
                message = dailyContext.sugarStatus() == OutGuideDto.IntakeStatus.HIGH
                        ? "오늘 당류 섭취량이 높은 편이라 달달한 음료와 디저트는 부담이 될 수 있어요."
                        : "카페 메뉴는 음료와 디저트에서 당류가 높아질 수 있어요.";
            }
            case "샐러드/포케" -> {
                tags.add("소스");
                level = OutGuideDto.RiskLevel.LOW;
                message = "샐러드와 포케는 비교적 선택이 쉬운 편이에요. 단백질 토핑과 소스 양만 확인해보세요.";
            }
            case "죽" -> {
                tags.add("탄수화물");
                level = dailyContext.carbsStatus() == OutGuideDto.IntakeStatus.HIGH
                        ? OutGuideDto.RiskLevel.CAUTION
                        : OutGuideDto.RiskLevel.LOW;
                message = "죽은 가볍게 먹기 좋지만 단백질이 부족하거나 탄수화물 중심이 될 수 있어요.";
            }
            case "한식" -> {
                tags.add("나트륨");
                level = dailyContext.sodiumStatus() == OutGuideDto.IntakeStatus.HIGH
                        ? OutGuideDto.RiskLevel.CAUTION
                        : OutGuideDto.RiskLevel.LOW;
                message = "한식은 메뉴 폭이 넓어요. 오늘 나트륨이 신경 쓰이면 국물과 짠 반찬을 줄이는 쪽이 좋아요.";
            }
            case "일식" -> {
                tags.add("탄수화물");
                tags.add("나트륨");
                level = elevatedLevel(dailyContext.carbsStatus(), dailyContext.sodiumStatus());
                message = "일식은 덮밥, 라멘, 돈카츠처럼 메뉴에 따라 부담이 달라져요. 밥과 국물 양을 같이 봐주세요.";
            }
            default -> {
                tags.add("메뉴선택");
                level = OutGuideDto.RiskLevel.CAUTION;
                message = "식당 카테고리만으로는 정확한 판단이 어려워요. 메뉴를 고를 때 밥, 면, 국물, 소스 양을 확인해보세요.";
            }
        }

        return new OutGuideDto.RiskResult(category, level, List.copyOf(tags), message);
    }

    private OutGuideDto.RiskLevel elevatedLevel(OutGuideDto.IntakeStatus first, OutGuideDto.IntakeStatus second) {
        if (first == OutGuideDto.IntakeStatus.HIGH && second == OutGuideDto.IntakeStatus.HIGH) {
            return OutGuideDto.RiskLevel.HIGH;
        }
        if (first == OutGuideDto.IntakeStatus.HIGH || second == OutGuideDto.IntakeStatus.HIGH) {
            return OutGuideDto.RiskLevel.CAUTION;
        }
        return OutGuideDto.RiskLevel.CAUTION;
    }

    private OutGuideDto.IntakeStatus statusOf(BigDecimal value, BigDecimal highThreshold) {
        if (nz(value).compareTo(highThreshold) >= 0) {
            return OutGuideDto.IntakeStatus.HIGH;
        }
        return OutGuideDto.IntakeStatus.NORMAL;
    }

    private String normalizeCategory(String categoryDetail) {
        String value = categoryDetail == null ? "" : categoryDetail;

        if (containsAny(value, "분식", "김밥", "떡볶이")) return "분식";
        if (containsAny(value, "중식", "중국")) return "중식";
        if (containsAny(value, "패스트푸드", "햄버거", "버거")) return "패스트푸드";
        if (containsAny(value, "카페", "디저트", "커피")) return "카페";
        if (containsAny(value, "샐러드", "포케", "샌드위치")) return "샐러드/포케";
        if (containsAny(value, "죽")) return "죽";
        if (containsAny(value, "일식", "초밥", "라멘", "돈까스", "돈카츠", "우동")) return "일식";
        if (containsAny(value, "한식", "찌개", "백반", "국밥", "고기")) return "한식";

        return "기타";
    }

    private boolean containsAny(String value, String... needles) {
        for (String needle : needles) {
            if (value.contains(needle)) {
                return true;
            }
        }
        return false;
    }

    private BigDecimal nz(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value;
    }
}
