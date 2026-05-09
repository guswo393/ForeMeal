package com.meta.foremeal.health.api.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;
import java.util.List;

public class BloodPressureDto {

    public record CreateRequest(
            @NotNull LocalDateTime measuredAt,
            @NotNull @Min(1) @Max(300) Integer systolic,
            @NotNull @Min(1) @Max(200) Integer diastolic,
            @Min(1) @Max(250) Integer pulse,
            @Size(max = 1000) String memo
    ) {
    }

    public record Response(
            Long bloodPressureId,
            Long userId,
            LocalDateTime measuredAt,
            Integer systolic,
            Integer diastolic,
            Integer pulse,
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
