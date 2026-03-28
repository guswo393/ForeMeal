package com.meta.foremeal.health.service;

import com.meta.foremeal.health.api.dto.HealthProfileDto;
import com.meta.foremeal.health.domain.HealthProfile;
import com.meta.foremeal.health.exception.HealthProfileNotFoundException;
import com.meta.foremeal.health.repo.HealthProfileRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class HealthProfileService {

    private final HealthProfileRepository healthProfileRepository;

    public HealthProfileService(HealthProfileRepository healthProfileRepository) {
        this.healthProfileRepository = healthProfileRepository;
    }

    @Transactional
    public HealthProfileDto.Response upsert(Long loginUserId, HealthProfileDto.UpsertRequest req) {
        HealthProfile profile = healthProfileRepository.findByUserId(loginUserId)
                .map(existing -> {
                    existing.update(
                            req.hasDiabetes(),
                            req.hasHypertension(),
                            req.heightCm(),
                            req.weightKg(),
                            req.goal(),
                            req.allergyNotes(),
                            req.avoidIngredients()
                    );
                    return existing;
                })
                .orElseGet(() -> healthProfileRepository.save(
                        new HealthProfile(
                                loginUserId,
                                req.hasDiabetes(),
                                req.hasHypertension(),
                                req.heightCm(),
                                req.weightKg(),
                                req.goal(),
                                req.allergyNotes(),
                                req.avoidIngredients()
                        )
                ));

        return toResponse(profile);
    }

    @Transactional(readOnly = true)
    public HealthProfileDto.Response getMyProfile(Long loginUserId) {
        HealthProfile profile = healthProfileRepository.findByUserId(loginUserId)
                .orElseThrow(HealthProfileNotFoundException::new);

        return toResponse(profile);
    }

    private HealthProfileDto.Response toResponse(HealthProfile profile) {
        return new HealthProfileDto.Response(
                profile.getHealthProfileId(),
                profile.getUserId(),
                profile.isHasDiabetes(),
                profile.isHasHypertension(),
                profile.getHeightCm(),
                profile.getWeightKg(),
                profile.getGoal(),
                profile.getAllergyNotes(),
                profile.getAvoidIngredients()
        );
    }
}
