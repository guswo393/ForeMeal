package com.meta.foremeal.meallog.service;

import com.meta.foremeal.foodmaster.domain.FoodMasterEntity;
import com.meta.foremeal.foodmaster.repo.FoodMasterRepository;
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
    private final FoodMasterRepository foodRepository;

    public MealLogService(MealLogRepository mealLogRepository,
                          DailyIntakeSummaryRepository summaryRepository,
                          FoodMasterRepository foodRepository) {
        this.mealLogRepository = mealLogRepository;
        this.summaryRepository = summaryRepository;
        this.foodRepository = foodRepository;
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
        Nutrients total = new Nutrients(BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO);

        for (MealLogItem item : items) {
            if (item.getFoodId() == null) {
                continue;
            }

            FoodMasterEntity food = foodRepository.findById(item.getFoodId())
                    .orElseThrow(() -> new IllegalArgumentException("Food not found. foodId=" + item.getFoodId()));

            BigDecimal multiplier = nutrientMultiplier(item);
            total = total.add(new Nutrients(
                    multiply(food.getCalories(), multiplier),
                    multiply(food.getSodium(), multiplier),
                    multiply(food.getSugar(), multiplier),
                    multiply(food.getCarbs(), multiplier)
            ));
        }

        return total;
    }

    private BigDecimal nutrientMultiplier(MealLogItem item) {
        BigDecimal quantity = item.getQuantity() == null ? BigDecimal.ZERO : item.getQuantity();
        String unit = item.getUnit() == null ? "" : item.getUnit().trim().toLowerCase();

        if (unit.equals("g") || unit.equals("gram") || unit.equals("grams") || unit.equals("그램")) {
            return quantity.divide(BigDecimal.valueOf(100));
        }

        if (unit.equals("kg") || unit.equals("kilogram") || unit.equals("kilograms") || unit.equals("킬로그램")) {
            return quantity.multiply(BigDecimal.TEN);
        }

        return quantity;
    }

    private BigDecimal multiply(Double value, BigDecimal multiplier) {
        if (value == null) {
            return BigDecimal.ZERO;
        }

        return BigDecimal.valueOf(value).multiply(multiplier);
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

        Nutrients add(Nutrients other) {
            return new Nutrients(
                    this.calories.add(other.calories),
                    this.sodium.add(other.sodium),
                    this.sugar.add(other.sugar),
                    this.carbs.add(other.carbs)
            );
        }

        private static BigDecimal nz(BigDecimal v) {
            return v == null ? BigDecimal.ZERO : v;
        }
    }
}
