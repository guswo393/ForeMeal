package com.meta.foremeal.health.api;

import com.meta.foremeal.global.security.principal.CustomUserPrincipal;
import com.meta.foremeal.health.api.dto.HealthProfileDto;
import com.meta.foremeal.health.service.HealthProfileService;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/health-profile")
public class HealthProfileController {

    private final HealthProfileService healthProfileService;

    public HealthProfileController(HealthProfileService healthProfileService) {
        this.healthProfileService = healthProfileService;
    }

    @PostMapping
    public HealthProfileDto.Response upsert(@AuthenticationPrincipal CustomUserPrincipal principal,
                                            @RequestBody @Valid HealthProfileDto.UpsertRequest req) {
        return healthProfileService.upsert(principal.getUserId(), req);
    }

    @GetMapping("/me")
    public HealthProfileDto.Response getMyProfile(@AuthenticationPrincipal CustomUserPrincipal principal) {
        return healthProfileService.getMyProfile(principal.getUserId());
    }
}
