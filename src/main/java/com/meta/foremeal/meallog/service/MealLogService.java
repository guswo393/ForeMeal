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
import java.util.ArrayList;
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
        recalculateDailySummary(loginUserId, req.eatenAt().toLocalDate());

        return toResponse(saved);
    }

    @Transactional
    public MealLogDto.Response update(Long loginUserId, Long mealId, MealLogDto.CreateRequest req) {
        MealLog mealLog = findMyMealLog(loginUserId, mealId);
        LocalDate oldDate = mealLog.getEatenAt().toLocalDate();
        LocalDate newDate = req.eatenAt().toLocalDate();

        mealLog.update(req.eatenAt(), req.notes(), req.source(), req.recipeId());
        mealLog.replaceItems(toItems(loginUserId, req.items()));

        recalculateDailySummary(loginUserId, oldDate);
        if (!oldDate.equals(newDate)) {
            recalculateDailySummary(loginUserId, newDate);
        }

        return toResponse(mealLog);
    }

    @Transactional
    public void delete(Long loginUserId, Long mealId) {
        MealLog mealLog = findMyMealLog(loginUserId, mealId);
        LocalDate date = mealLog.getEatenAt().toLocalDate();

        mealLogRepository.delete(mealLog);
        mealLogRepository.flush();
        recalculateDailySummary(loginUserId, date);
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

    private MealLog findMyMealLog(Long loginUserId, Long mealId) {
        return mealLogRepository.findByMealIdAndUserId(mealId, loginUserId)
                .orElseThrow(() -> new IllegalArgumentException("Meal log not found. mealId=" + mealId));
    }

    private List<MealLogItem> toItems(Long loginUserId, List<MealLogDto.ItemRequest> itemRequests) {
        List<MealLogItem> items = new ArrayList<>();
        for (MealLogDto.ItemRequest it : itemRequests) {
            items.add(new MealLogItem(
                    loginUserId,
                    it.foodId(),
                    it.foodName(),
                    it.quantity(),
                    it.unit()
            ));
        }
        return items;
    }

    private void recalculateDailySummary(Long loginUserId, LocalDate date) {
        List<MealLog> mealLogs = mealLogRepository.findByUserIdAndEatenAtBetweenOrderByEatenAtAsc(
                loginUserId,
                date.atStartOfDay(),
                date.plusDays(1).atStartOfDay()
        );

        Nutrients total = new Nutrients(BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO);
        for (MealLog mealLog : mealLogs) {
            total = total.add(calculateNutrients(mealLog.getItems()));
        }

        DailyIntakeSummary summary = summaryRepository
                .findByUserIdAndSummaryDate(loginUserId, date)
                .orElseGet(() -> summaryRepository.save(new DailyIntakeSummary(loginUserId, date)));

        summary.updateTotals(total.calories, total.sodium, total.sugar, total.carbs);
    }

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

        if (unit.equals("g") || unit.equals("gram") || unit.equals("grams")) {
            return quantity.divide(BigDecimal.valueOf(100));
        }

        if (unit.equals("kg") || unit.equals("kilogram") || unit.equals("kilograms")) {
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
