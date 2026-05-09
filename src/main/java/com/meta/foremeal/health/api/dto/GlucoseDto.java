package com.meta.foremeal.health.api.dto;

import com.meta.foremeal.health.domain.GlucoseMeasureType;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public class GlucoseDto {

    public record CreateRequest(
            @NotNull LocalDateTime measuredAt,
            @NotNull @DecimalMin(value = "0.0", inclusive = false) BigDecimal glucoseValue,
            @NotNull GlucoseMeasureType measureType,
            @Size(max = 1000) String memo
    ) {
    }

    public record Response(
            Long glucoseId,
            Long userId,
            LocalDateTime measuredAt,
            BigDecimal glucoseValue,
            GlucoseMeasureType measureType,
            String memo
    ) {
    }

    public record DailyResponse(
            Long userId,
            String date,
            List<Response> records
    ) {
    }
}