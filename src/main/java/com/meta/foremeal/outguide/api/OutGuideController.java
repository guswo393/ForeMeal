package com.meta.foremeal.outguide.api;

import com.meta.foremeal.global.security.principal.CustomUserPrincipal;
import com.meta.foremeal.outguide.dto.OutGuideDto;
import com.meta.foremeal.outguide.service.OutGuideService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.time.LocalDate;

@RestController
@RequestMapping("/api/out-guide")
public class OutGuideController {

    private final OutGuideService outGuideService;

    public OutGuideController(OutGuideService outGuideService) {
        this.outGuideService = outGuideService;
    }

    @GetMapping("/restaurants")
    public OutGuideDto.RestaurantSearchResponse searchRestaurants(
            @AuthenticationPrincipal CustomUserPrincipal principal,
            @RequestParam BigDecimal lat,
            @RequestParam BigDecimal lng,
            @RequestParam(required = false) Integer radius,
            @RequestParam(required = false) Integer size,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date
    ) {
        if (principal == null) {
            throw new IllegalArgumentException("Login is required to use out guide.");
        }

        return outGuideService.searchRestaurants(
                principal.getUserId(),
                lat,
                lng,
                radius,
                size,
                date
        );
    }
}
