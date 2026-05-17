package com.meta.foremeal.recipe.service;

import com.meta.foremeal.recipe.dto.ConversionDto;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class ConversionServiceTest {

    private final ConversionService conversionService = new ConversionService();

    @Test
    void returnsIngredientNamesInDisplayOrder() {
        List<ConversionDto.IngredientResponse> ingredients = conversionService.getIngredients();

        assertThat(ingredients)
                .extracting(ConversionDto.IngredientResponse::name)
                .containsExactly("설탕", "흑설탕", "물엿", "올리고당", "꿀", "미림");
    }

    @Test
    void returnsSubstitutesForSelectedIngredient() {
        List<ConversionDto.SubstituteResponse> substitutes = conversionService.getSubstitutes("설탕");

        assertThat(substitutes)
                .extracting(ConversionDto.SubstituteResponse::name)
                .containsExactly(
                        "1:1 대체형 일반 요리용 스테비아",
                        "감미도 10.0 고농축 분말스테비아",
                        "알룰로스",
                        "저당 올리고당"
                );
    }

    @Test
    void calculatesConvertedGramByInputGramAndRatio() {
        ConversionDto.CalculateResponse response = conversionService.calculate(
                new ConversionDto.CalculateRequest("설탕", "알룰로스", new BigDecimal("10"))
        );

        assertThat(response.convertedGram()).isEqualByComparingTo("14.3");
        assertThat(response.resultText()).isEqualTo("설탕 10g → 알룰로스 14.3g");
        assertThat(response.description()).contains("설탕과 풍미가 가장 가까운 대체재");
    }

    @Test
    void keepsSmallConvertedGramPrecision() {
        ConversionDto.CalculateResponse response = conversionService.calculate(
                new ConversionDto.CalculateRequest("미림", "감미도 10.0 고농축 분말스테비아", new BigDecimal("10"))
        );

        assertThat(response.convertedGram()).isEqualByComparingTo("0.375");
        assertThat(response.resultText()).isEqualTo("미림 10g → 감미도 10.0 고농축 분말스테비아 0.375g");
    }

    @Test
    void throwsExceptionForUnsupportedIngredient() {
        assertThatThrownBy(() -> conversionService.getSubstitutes("간장"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("지원하지 않는 기준 재료");
    }

    @Test
    void throwsExceptionForUnsupportedSubstitute() {
        assertThatThrownBy(() -> conversionService.calculate(
                new ConversionDto.CalculateRequest("설탕", "에리스리톨", new BigDecimal("10"))
        ))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("지원하지 않는 대체 감미료");
    }
}
