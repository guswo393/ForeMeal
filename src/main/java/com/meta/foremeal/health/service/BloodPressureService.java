package com.meta.foremeal.health.service;

import com.meta.foremeal.health.api.dto.BloodPressureDto;
import com.meta.foremeal.health.domain.BloodPressure;
import com.meta.foremeal.health.repo.BloodPressureRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class BloodPressureService {

    private final BloodPressureRepository bloodPressureRepository;

    public BloodPressureService(BloodPressureRepository bloodPressureRepository) {
        this.bloodPressureRepository = bloodPressureRepository;
    }

    @Transactional
    public BloodPressureDto.Response create(Long loginUserId, BloodPressureDto.CreateRequest req) {
        BloodPressure bloodPressure = new BloodPressure(
                loginUserId,
                req.measuredAt(),
                req.systolic(),
                req.diastolic(),
                req.pulse(),
                req.memo()
        );

        BloodPressure saved = bloodPressureRepository.save(bloodPressure);
        return toResponse(saved);
    }

    @Transactional(readOnly = true)
    public BloodPressureDto.DailyResponse getDaily(Long loginUserId, LocalDate date) {
        LocalDateTime from = date.atStartOfDay();
        LocalDateTime to = date.plusDays(1).atStartOfDay();

        List<BloodPressureDto.Response> records = bloodPressureRepository
                .findByUserIdAndMeasuredAtBetweenOrderByMeasuredAtAsc(loginUserId, from, to)
                .stream()
                .map(this::toResponse)
                .toList();

        return new BloodPressureDto.DailyResponse(loginUserId, date.toString(), records);
    }

    private BloodPressureDto.Response toResponse(BloodPressure bloodPressure) {
        return new BloodPressureDto.Response(
                bloodPressure.getBloodPressureId(),
                bloodPressure.getUserId(),
                bloodPressure.getMeasuredAt(),
                bloodPressure.getSystolic(),
                bloodPressure.getDiastolic(),
                bloodPressure.getPulse(),
                bloodPressure.getMemo()
        );
    }
}