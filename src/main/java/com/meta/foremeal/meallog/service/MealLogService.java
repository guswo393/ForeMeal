package com.meta.foremeal.meallog.service;

import com.meta.foremeal.meallog.api.dto.MealLogDto;
import com.meta.foremeal.meallog.domain.MealLog;
import com.meta.foremeal.meallog.repo.MealLogRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class MealLogService {

    private final MealLogRepository mealLogRepository;

    public MealLogService(MealLogRepository mealLogRepository) {
        this.mealLogRepository = mealLogRepository;
    }

    @Transactional
    public MealLogDto.Response create(MealLogDto.CreateRequest req) {
        MealLog mealLog = new MealLog(
                req.userId(),
                req.eatenAt(),
                req.notes(),
                req.source(),
                req.recipeId()
        );
        MealLog saved = mealLogRepository.save(mealLog);

        return new MealLogDto.Response(
                saved.getMealId(),
                saved.getUserId(),
                saved.getEatenAt(),
                saved.getNotes(),
                saved.getSource(),
                saved.getRecipeId()
        );
    }

    @Transactional(readOnly = true)
    public List<MealLogDto.Response> getDaily(Long userId, LocalDate date) {
        LocalDateTime from = date.atStartOfDay();
        LocalDateTime to = date.plusDays(1).atStartOfDay();

        return mealLogRepository.findByUserIdAndEatenAtBetweenOrderByEatenAtAsc(userId, from, to)
                .stream()
                .map(m -> new MealLogDto.Response(
                        m.getMealId(),
                        m.getUserId(),
                        m.getEatenAt(),
                        m.getNotes(),
                        m.getSource(),
                        m.getRecipeId()
                ))
                .toList();
    }
}