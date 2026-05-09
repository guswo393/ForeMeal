package com.meta.foremeal.health.service;

import com.meta.foremeal.health.api.dto.GlucoseDto;
import com.meta.foremeal.health.domain.Glucose;
import com.meta.foremeal.health.repo.GlucoseRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class GlucoseService {

    private final GlucoseRepository glucoseRepository;

    public GlucoseService(GlucoseRepository glucoseRepository) {
        this.glucoseRepository = glucoseRepository;
    }

    @Transactional
    public GlucoseDto.Response create(Long loginUserId, GlucoseDto.CreateRequest req) {
        Glucose glucose = new Glucose(
                loginUserId,
                req.measuredAt(),
                req.glucoseValue(),
                req.measureType(),
                req.memo()
        );

        Glucose saved = glucoseRepository.save(glucose);
        return toResponse(saved);
    }

    @Transactional(readOnly = true)
    public GlucoseDto.DailyResponse getDaily(Long loginUserId, LocalDate date) {
        LocalDateTime from = date.atStartOfDay();
        LocalDateTime to = date.plusDays(1).atStartOfDay();

        List<GlucoseDto.Response> records = glucoseRepository
                .findByUserIdAndMeasuredAtBetweenOrderByMeasuredAtAsc(loginUserId, from, to)
                .stream()
                .map(this::toResponse)
                .toList();

        return new GlucoseDto.DailyResponse(loginUserId, date.toString(), records);
    }

    private GlucoseDto.Response toResponse(Glucose glucose) {
        return new GlucoseDto.Response(
                glucose.getGlucoseId(),
                glucose.getUserId(),
                glucose.getMeasuredAt(),
                glucose.getGlucoseValue(),
                glucose.getMeasureType(),
                glucose.getMemo()
        );
    }
}
