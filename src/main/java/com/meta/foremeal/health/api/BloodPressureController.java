package com.meta.foremeal.health.api;

import com.meta.foremeal.global.security.principal.CustomUserPrincipal;
import com.meta.foremeal.health.api.dto.BloodPressureDto;
import com.meta.foremeal.health.service.BloodPressureService;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/blood-pressure")
public class BloodPressureController {

    private final BloodPressureService bloodPressureService;

    public BloodPressureController(BloodPressureService bloodPressureService) {
        this.bloodPressureService = bloodPressureService;
    }

    @PostMapping
    public BloodPressureDto.Response create(@AuthenticationPrincipal CustomUserPrincipal principal,
                                            @RequestBody @Valid BloodPressureDto.CreateRequest req) {
        return bloodPressureService.create(principal.getUserId(), req);
    }

    @GetMapping("/daily")
    public BloodPressureDto.DailyResponse getDaily(@AuthenticationPrincipal CustomUserPrincipal principal,
                                                   @RequestParam String date) {
        return bloodPressureService.getDaily(principal.getUserId(), LocalDate.parse(date));
    }
}