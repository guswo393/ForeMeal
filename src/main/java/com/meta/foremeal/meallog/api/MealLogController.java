package com.meta.foremeal.meallog.api;

import com.meta.foremeal.meallog.api.dto.MealLogDto;
import com.meta.foremeal.meallog.service.MealLogService;
import jakarta.validation.Valid;
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
    public MealLogDto.Response create(@RequestBody @Valid MealLogDto.CreateRequest req) {
        return mealLogService.create(req);
    }

    @GetMapping("/daily")
    public List<MealLogDto.Response> getDaily(@RequestParam Long userId, @RequestParam String date) {
        return mealLogService.getDaily(userId, LocalDate.parse(date));
    }

    @GetMapping("/daily-summary")
    public MealLogDto.DailySummaryResponse getDailySummary(@RequestParam Long userId, @RequestParam String date) {
        return mealLogService.getDailySummary(userId, LocalDate.parse(date));
    }
}