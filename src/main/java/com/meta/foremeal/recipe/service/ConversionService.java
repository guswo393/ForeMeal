package com.meta.foremeal.recipe.service;

import com.meta.foremeal.recipe.dto.ConversionDto;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class ConversionService {

    private static final Map<String, IngredientConversion> CONVERSIONS = createConversions();

    public List<ConversionDto.IngredientResponse> getIngredients() {
        return CONVERSIONS.values().stream()
                .map(it -> new ConversionDto.IngredientResponse(it.name()))
                .toList();
    }

    public List<ConversionDto.SubstituteResponse> getSubstitutes(String ingredient) {
        IngredientConversion conversion = findIngredient(ingredient);

        return conversion.substitutes().values().stream()
                .map(it -> new ConversionDto.SubstituteResponse(it.name(), it.ratio()))
                .toList();
    }

    public ConversionDto.CalculateResponse calculate(ConversionDto.CalculateRequest request) {
        IngredientConversion ingredient = findIngredient(request.ingredient());
        SubstituteConversion substitute = findSubstitute(ingredient, request.substitute());

        BigDecimal inputGram = normalize(request.gram());
        BigDecimal convertedGram = normalize(inputGram.multiply(substitute.ratio()));
        String inputGramText = formatGram(inputGram);
        String convertedGramText = formatGram(convertedGram);

        return new ConversionDto.CalculateResponse(
                ingredient.name(),
                substitute.name(),
                inputGram,
                convertedGram,
                inputGramText,
                convertedGramText,
                ingredient.name() + " " + inputGramText + " → " + substitute.name() + " " + convertedGramText,
                substitute.description()
        );
    }

    private IngredientConversion findIngredient(String ingredient) {
        IngredientConversion conversion = CONVERSIONS.get(ingredient);
        if (conversion == null) {
            throw new IllegalArgumentException("지원하지 않는 기준 재료입니다: " + ingredient);
        }
        return conversion;
    }

    private SubstituteConversion findSubstitute(IngredientConversion ingredient, String substitute) {
        SubstituteConversion conversion = ingredient.substitutes().get(substitute);
        if (conversion == null) {
            throw new IllegalArgumentException("해당 기준 재료에서 지원하지 않는 대체 감미료입니다: " + substitute);
        }
        return conversion;
    }

    private static Map<String, IngredientConversion> createConversions() {
        Map<String, IngredientConversion> conversions = new LinkedHashMap<>();

        conversions.put("설탕", ingredient("설탕",
                substitute("1:1 대체형 일반 요리용 스테비아", "1",
                        "시중 스테비아 제품(에리스리톨 혼합) 기준이에요. 제품마다 단맛 강도가 다를 수 있으니 구매 전 포장지의 '설탕 대비 사용량'을 꼭 확인해 주세요! 처음엔 계산값의 절반부터 시작해서 맛을 보며 조금씩 늘려가면 딱 맞는 단맛을 찾을 수 있어요."),
                substitute("감미도 10.0 고농축 분말스테비아", "0.1",
                        "소량으로도 강한 단맛을 내는 고농축 제품 기준이에요. 포장지에 '설탕의 몇 배' 또는 '몇 g당 설탕 몇 g 대체'라고 적혀 있으니 꼭 확인 후 사용해 주세요. 처음엔 계산값보다 적게 넣고 맛을 보며 조절하는 걸 추천해요!"),
                substitute("알룰로스", "1.43",
                        "설탕과 풍미가 가장 가까운 대체재예요. 단맛이 조금 부드러워서 계산값보다 살짝 더 넣으면 더 익숙한 맛이 나요."),
                substitute("저당 올리고당", "1.6",
                        "액상 타입이라 양이 조금 늘지만, 당 부담을 낮추면서 자연스러운 단맛을 낼 수 있어요. 소스나 조림 요리에 특히 잘 맞아요!")
        ));

        conversions.put("흑설탕", ingredient("흑설탕",
                substitute("1:1 대체형 일반 요리용 스테비아", "1",
                        "단맛은 충분히 대체돼요. 흑설탕의 깊은 풍미가 그리울 땐 시나몬 한 꼬집을 더해보면 비슷한 분위기를 낼 수 있어요. 포장지의 사용량 안내도 꼭 확인해 주세요!"),
                substitute("감미도 10.0 고농축 분말스테비아", "0.1",
                        "고농축 제품은 조금만 넣어도 강하게 달 수 있어요. 포장지에 표시된 설탕 대비 사용량을 먼저 확인하고, 처음엔 계산값의 절반부터 시작해 보세요."),
                substitute("알룰로스", "1.43",
                        "단맛 대체에 가장 무난한 선택이에요. 흑설탕의 색과 풍미는 조금 달라지지만, 촉촉한 질감은 잘 살려줘요.")
        ));

        conversions.put("물엿", ingredient("물엿",
                substitute("1:1 대체형 일반 요리용 스테비아", "0.4",
                        "단맛은 대체되지만 점도는 따로 보완이 필요해요. 물이나 전분을 소량 추가하면 원하는 질감에 가깝게 조절할 수 있어요. 포장지의 사용량 안내도 꼭 확인해 주세요!"),
                substitute("감미도 10.0 고농축 분말스테비아", "0.04",
                        "아주 소량으로 단맛을 낼 수 있어요. 물엿의 윤기와 점도는 대체되지 않으니 필요하다면 물이나 전분으로 보완해 주세요. 포장지 사용량 기준을 꼭 먼저 확인해 주세요!"),
                substitute("알룰로스", "0.53",
                        "액상 단맛 대체에 가장 잘 맞는 선택이에요. 물엿보다 조금 묽지만 윤기는 충분히 살릴 수 있어요."),
                substitute("저당 올리고당", "0.67",
                        "질감과 윤기가 물엿과 가장 비슷해서 자연스럽게 대체할 수 있어요. 조림이나 볶음 요리에 추천해요!")
        ));

        conversions.put("올리고당", ingredient("올리고당",
                substitute("1:1 대체형 일반 요리용 스테비아", "0.6",
                        "단맛을 가볍게 줄이고 싶을 때 좋은 선택이에요. 액상 느낌이 필요한 요리엔 물을 소량 섞어 사용해보세요. 포장지의 사용량 안내도 꼭 확인해 주세요!"),
                substitute("감미도 10.0 고농축 분말스테비아", "0.06",
                        "소량으로 올리고당의 단맛을 낼 수 있어요. 액상 질감이 필요한 요리라면 물을 살짝 더해 보완해 주세요. 포장지 기준량도 꼭 확인해 주세요!"),
                substitute("알룰로스", "0.8",
                        "당류 부담을 낮추면서도 액상 단맛을 자연스럽게 낼 수 있어요. 올리고당과 사용감이 비슷해서 적응이 쉬워요!"),
                substitute("저당 올리고당", "1",
                        "가장 자연스러운 대체예요. 같은 양부터 시작해서 단맛을 보며 조절하면 거의 차이를 못 느낄 수 있어요!")
        ));

        conversions.put("꿀", ingredient("꿀",
                substitute("1:1 대체형 일반 요리용 스테비아", "1.05",
                        "단맛은 충분히 대체돼요. 꿀 특유의 향이 포인트인 요리라면 레몬즙이나 바닐라를 살짝 더해보는 것도 좋아요. 포장지의 사용량 안내도 꼭 확인해 주세요!"),
                substitute("감미도 10.0 고농축 분말스테비아", "0.105",
                        "꿀보다 훨씬 적은 양으로 단맛을 낼 수 있어요. 꿀 특유의 향은 대체되지 않으니 바닐라나 레몬즙으로 보완해 보세요. 포장지 기준량 꼭 확인해 주세요!"),
                substitute("알룰로스", "1.5",
                        "꿀보다 향은 가볍지만, 당 부담 없이 촉촉한 단맛을 낼 수 있어요. 베이킹이나 드레싱에 특히 잘 어울려요!")
        ));

        conversions.put("미림", ingredient("미림",
                substitute("1:1 대체형 일반 요리용 스테비아", "0.375",
                        "단맛은 대체할 수 있어요. 잡내 제거가 필요한 요리엔 청주나 생강을 함께 활용하면 미림과 비슷한 효과를 낼 수 있어요. 포장지의 사용량 안내도 꼭 확인해 주세요!"),
                substitute("감미도 10.0 고농축 분말스테비아", "0.0375",
                        "아주 소량으로 미림의 단맛을 낼 수 있어요. 잡내 제거가 필요하다면 청주나 생강을 꼭 함께 사용해 주세요. 포장지 기준량도 먼저 확인해 주세요."),
                substitute("알룰로스", "0.5",
                        "미림의 단맛을 건강하게 대체할 수 있어요. 잡내 제거가 중요한 요리라면 청주를 소량 함께 넣으면 더 좋아요."),
                substitute("저당 올리고당", "0.62",
                        "단맛과 윤기를 함께 살릴 수 있어서 조림이나 구이에 잘 맞아요. 알코올 향이 필요하다면 청주를 살짝 더해보세요!")
        ));

        return Collections.unmodifiableMap(conversions);
    }

    private static IngredientConversion ingredient(String name, SubstituteConversion... substitutes) {
        Map<String, SubstituteConversion> substituteMap = new LinkedHashMap<>();
        for (SubstituteConversion substitute : substitutes) {
            substituteMap.put(substitute.name(), substitute);
        }
        return new IngredientConversion(name, Collections.unmodifiableMap(substituteMap));
    }

    private static SubstituteConversion substitute(String name, String ratio, String description) {
        return new SubstituteConversion(name, new BigDecimal(ratio), description);
    }

    private BigDecimal normalize(BigDecimal value) {
        return value.setScale(4, RoundingMode.HALF_UP).stripTrailingZeros();
    }

    private String formatGram(BigDecimal gram) {
        return normalize(gram).toPlainString() + "g";
    }

    private record IngredientConversion(
            String name,
            Map<String, SubstituteConversion> substitutes
    ) {}

    private record SubstituteConversion(
            String name,
            BigDecimal ratio,
            String description
    ) {}
}
