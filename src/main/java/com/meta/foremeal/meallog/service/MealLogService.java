package com.meta.foremeal.meallog.service;

import com.meta.foremeal.meallog.api.dto.MealLogDto;
import com.meta.foremeal.meallog.domain.DailyIntakeSummary;
import com.meta.foremeal.meallog.domain.MealLog;
import com.meta.foremeal.meallog.domain.MealLogItem;
import com.meta.foremeal.meallog.repo.DailyIntakeSummaryRepository;
import com.meta.foremeal.meallog.repo.MealLogRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class MealLogService {

    private final MealLogRepository mealLogRepository;
    private final DailyIntakeSummaryRepository summaryRepository;

    public MealLogService(MealLogRepository mealLogRepository,
                          DailyIntakeSummaryRepository summaryRepository) {
        this.mealLogRepository = mealLogRepository;
        this.summaryRepository = summaryRepository;
    }

    @Transactional
    public MealLogDto.Response create(Long loginUserId, MealLogDto.CreateRequest req) {
        MealLog mealLog = new MealLog(
                loginUserId,
                req.eatenAt(),
                req.notes(),
                req.source(),
                req.recipeId()
        );

        for (MealLogDto.ItemRequest it : req.items()) {
            MealLogItem item = new MealLogItem(
                    loginUserId,
                    it.foodId(),
                    it.foodName(),
                    it.quantity(),
                    it.unit()
            );
            mealLog.addItem(item);
        }

        MealLog saved = mealLogRepository.save(mealLog);

        LocalDate date = req.eatenAt().toLocalDate();
        DailyIntakeSummary summary = summaryRepository
                .findByUserIdAndSummaryDate(loginUserId, date)
                .orElseGet(() -> summaryRepository.save(new DailyIntakeSummary(loginUserId, date)));

        Nutrients nutrients = calculateNutrients(saved.getItems());
        summary.add(nutrients.calories, nutrients.sodium, nutrients.sugar, nutrients.carbs);

        return toResponse(saved);
    }

    @Transactional(readOnly = true)
    public List<MealLogDto.Response> getDaily(Long loginUserId, LocalDate date) {
        LocalDateTime from = date.atStartOfDay();
        LocalDateTime to = date.plusDays(1).atStartOfDay();

        return mealLogRepository.findByUserIdAndEatenAtBetweenOrderByEatenAtAsc(loginUserId, from, to)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public MealLogDto.DailySummaryResponse getDailySummary(Long loginUserId, LocalDate date) {
        DailyIntakeSummary s = summaryRepository
                .findByUserIdAndSummaryDate(loginUserId, date)
                .orElseGet(() -> new DailyIntakeSummary(loginUserId, date));

        return new MealLogDto.DailySummaryResponse(
                loginUserId,
                date.toString(),
                s.getTotalCalories(),
                s.getTotalSodium(),
                s.getTotalSugar(),
                s.getTotalCarbs()
        );
    }

    private MealLogDto.Response toResponse(MealLog m) {
        List<MealLogDto.ItemResponse> items = m.getItems().stream()
                .map(i -> new MealLogDto.ItemResponse(
                        i.getItemId(),
                        i.getFoodId(),
                        i.getFoodName(),
                        i.getQuantity(),
                        i.getUnit()
                ))
                .toList();

        return new MealLogDto.Response(
                m.getMealId(),
                m.getUserId(),
                m.getEatenAt(),
                m.getNotes(),
                m.getSource(),
                m.getRecipeId(),
                items
        );
    }

    /**
     * 지금은 누적 구조만 완성하는 단계라 최소 구현:
     * - FoodMaster 연동되면 여기에서 foodId + quantity로 영양값 계산해서 반환하면 됨
     */
    private Nutrients calculateNutrients(List<MealLogItem> items) {
        return new Nutrients(BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO);
    }

    private static class Nutrients {
        final BigDecimal calories;
        final BigDecimal sodium;
        final BigDecimal sugar;
        final BigDecimal carbs;

        Nutrients(BigDecimal calories, BigDecimal sodium, BigDecimal sugar, BigDecimal carbs) {
            this.calories = nz(calories);
            this.sodium = nz(sodium);
            this.sugar = nz(sugar);
            this.carbs = nz(carbs);
        }

        private static BigDecimal nz(BigDecimal v) {
            return v == null ? BigDecimal.ZERO : v;
        }
    }
}