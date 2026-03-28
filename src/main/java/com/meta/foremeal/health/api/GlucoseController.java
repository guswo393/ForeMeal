package com.meta.foremeal.health.api;

import com.meta.foremeal.global.security.principal.CustomUserPrincipal;
import com.meta.foremeal.health.api.dto.GlucoseDto;
import com.meta.foremeal.health.service.GlucoseService;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/glucose")
public class GlucoseController {

    private final GlucoseService glucoseService;

    public GlucoseController(GlucoseService glucoseService) {
        this.glucoseService = glucoseService;
    }

    @PostMapping
    public GlucoseDto.Response create(@AuthenticationPrincipal CustomUserPrincipal principal,
                                      @RequestBody @Valid GlucoseDto.CreateRequest req) {
        return glucoseService.create(principal.getUserId(), req);
    }

    @GetMapping("/daily")
    public GlucoseDto.DailyResponse getDaily(@AuthenticationPrincipal CustomUserPrincipal principal,
                                             @RequestParam String date) {
        return glucoseService.getDaily(principal.getUserId(), LocalDate.parse(date));
    }
}