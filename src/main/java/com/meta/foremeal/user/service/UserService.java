package com.meta.foremeal.user.service;

import com.meta.foremeal.meallog.repo.DailyIntakeSummaryRepository;
import com.meta.foremeal.meallog.repo.MealLogItemRepository;
import com.meta.foremeal.meallog.repo.MealLogRepository;
import com.meta.foremeal.user.domain.User;
import com.meta.foremeal.user.domain.UserRole;
import com.meta.foremeal.user.dto.UserDto;
import com.meta.foremeal.user.exception.DuplicateEmailException;
import com.meta.foremeal.user.exception.InvalidPasswordException;
import com.meta.foremeal.user.exception.UserNotFoundException;
import com.meta.foremeal.user.repo.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;

@Service
@Transactional
public class UserService {

    private static final Duration PASSWORD_CHANGE_INTERVAL = Duration.ofDays(7);

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final MealLogRepository mealLogRepository;
    private final MealLogItemRepository mealLogItemRepository;
    private final DailyIntakeSummaryRepository dailyIntakeSummaryRepository;

    public UserService(UserRepository userRepository,
                       PasswordEncoder passwordEncoder,
                       MealLogRepository mealLogRepository,
                       MealLogItemRepository mealLogItemRepository,
                       DailyIntakeSummaryRepository dailyIntakeSummaryRepository) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.mealLogRepository = mealLogRepository;
        this.mealLogItemRepository = mealLogItemRepository;
        this.dailyIntakeSummaryRepository = dailyIntakeSummaryRepository;
    }

    public UserDto.Response create(UserDto.CreateRequest request) {
        if (userRepository.existsByEmail(request.email())) {
            throw new DuplicateEmailException(request.email());
        }

        String encodedPassword = passwordEncoder.encode(request.password());

        User user = new User(
                request.email(),
                encodedPassword,
                request.username(),
                request.birthDate(),
                UserRole.USER
        );

        User savedUser = userRepository.save(user);
        return toResponse(savedUser);
    }

    @Transactional(readOnly = true)
    public UserDto.Response getById(Long userId) {
        User user = findUser(userId);
        return toResponse(user);
    }

    public UserDto.Response update(Long userId, UserDto.UpdateRequest request) {
        User user = findUser(userId);
        user.update(request.username(), request.birthDate());
        return toResponse(user);
    }

    public void changePassword(Long userId, UserDto.ChangePasswordRequest request) {
        User user = findUser(userId);
        validatePassword(user, request.currentPassword());
        validatePasswordChangeInterval(user);
        user.changePassword(passwordEncoder.encode(request.newPassword()));
    }

    public void delete(Long userId, UserDto.DeleteRequest request) {
        User user = findUser(userId);
        validatePassword(user, request.password());

        mealLogItemRepository.deleteByUserId(userId);
        mealLogRepository.deleteByUserId(userId);
        dailyIntakeSummaryRepository.deleteByUserId(userId);
        userRepository.delete(user);
    }

    private User findUser(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));
    }

    private void validatePassword(User user, String rawPassword) {
        if (!passwordEncoder.matches(rawPassword, user.getPassword())) {
            throw new InvalidPasswordException();
        }
    }

    private void validatePasswordChangeInterval(User user) {
        LocalDateTime passwordChangedAt = user.getPasswordChangedAt();
        if (passwordChangedAt == null) {
            return;
        }

        LocalDateTime nextAllowedAt = passwordChangedAt.plus(PASSWORD_CHANGE_INTERVAL);
        if (LocalDateTime.now().isBefore(nextAllowedAt)) {
            throw new IllegalStateException("비밀번호는 7일에 한 번만 변경할 수 있습니다.");
        }
    }

    private UserDto.Response toResponse(User user) {
        return new UserDto.Response(
                user.getUserId(),
                user.getEmail(),
                user.getUsername(),
                user.getBirthYear(),
                user.getBirthDate()
        );
    }
}
