package com.meta.foremeal.user.service;

import com.meta.foremeal.health.domain.HealthProfile;
import com.meta.foremeal.health.repo.HealthProfileRepository;
import com.meta.foremeal.user.domain.User;
import com.meta.foremeal.user.dto.MyPageDto;
import com.meta.foremeal.user.exception.UserNotFoundException;
import com.meta.foremeal.user.repo.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class MyPageService {

    private final UserRepository userRepository;
    private final HealthProfileRepository healthProfileRepository;

    public MyPageService(UserRepository userRepository,
                         HealthProfileRepository healthProfileRepository) {
        this.userRepository = userRepository;
        this.healthProfileRepository = healthProfileRepository;
    }

    @Transactional(readOnly = true)
    public MyPageDto.Response getMyPage(Long userId) {
        User user = findUser(userId);
        HealthProfile profile = healthProfileRepository.findByUserId(userId).orElse(null);

        return toResponse(user, profile);
    }

    @Transactional
    public MyPageDto.Response updateMyPage(Long userId, MyPageDto.UpdateRequest request) {
        User user = findUser(userId);
        user.update(request.username(), request.birthDate());

        HealthProfile profile = healthProfileRepository.findByUserId(userId)
                .map(existing -> {
                    existing.updateBodyMeasurements(request.heightCm(), request.weightKg());
                    return existing;
                })
                .orElseGet(() -> healthProfileRepository.save(
                        new HealthProfile(
                                userId,
                                false,
                                false,
                                request.heightCm(),
                                request.weightKg(),
                                null,
                                null,
                                null
                        )
                ));

        return toResponse(user, profile);
    }

    private User findUser(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));
    }

    private MyPageDto.Response toResponse(User user, HealthProfile profile) {
        return new MyPageDto.Response(
                user.getUserId(),
                user.getEmail(),
                user.getUsername(),
                user.getBirthDate(),
                user.getBirthYear(),
                profile == null ? null : profile.getHeightCm(),
                profile == null ? null : profile.getWeightKg()
        );
    }
}
