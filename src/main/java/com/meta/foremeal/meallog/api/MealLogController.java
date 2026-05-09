package com.meta.foremeal.meallog.api;

import com.meta.foremeal.global.security.principal.CustomUserPrincipal;
import com.meta.foremeal.meallog.api.dto.MealLogDto;
import com.meta.foremeal.meallog.service.MealLogService;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/meal-logs")
public class MealLogController {

    private final MealLogService mealLogService;

    public MealLogController(MealLogService mealLogService) {
        this.mealLogService = mealLogService;
    }

    @PostMapping
    public MealLogDto.Response create(@AuthenticationPrincipal CustomUserPrincipal principal,
                                      @RequestBody @Valid MealLogDto.CreateRequest req) {
        return mealLogService.create(principal.getUserId(), req);
    }

    @GetMapping("/daily")
    public List<MealLogDto.Response> getDaily(@AuthenticationPrincipal CustomUserPrincipal principal,
                                              @RequestParam String date) {
        return mealLogService.getDaily(principal.getUserId(), LocalDate.parse(date));
    }

    @GetMapping("/daily-summary")
    public MealLogDto.DailySummaryResponse getDailySummary(@AuthenticationPrincipal CustomUserPrincipal principal,
                                                           @RequestParam String date) {
        return mealLogService.getDailySummary(principal.getUserId(), LocalDate.parse(date));
    }
}