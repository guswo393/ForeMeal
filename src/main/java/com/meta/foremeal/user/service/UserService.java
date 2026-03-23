package com.meta.foremeal.user.service;

import com.meta.foremeal.user.domain.User;
import com.meta.foremeal.user.dto.UserDto;
import com.meta.foremeal.user.repo.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class UserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public UserDto.Response create(UserDto.CreateRequest request) {
        if (userRepository.existsByEmail(request.email())) {
            throw new IllegalArgumentException("이미 사용 중인 이메일입니다.");
        }

        User user = new User(
                request.email(),
                request.password(),
                request.username(),
                request.birthYear()
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
        user.update(request.username(), request.birthYear());
        return toResponse(user);
    }

    public void changePassword(Long userId, UserDto.ChangePasswordRequest request) {
        User user = findUser(userId);
        user.changePassword(request.password());
    }

    private User findUser(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("해당 사용자를 찾을 수 없습니다."));
    }

    private UserDto.Response toResponse(User user) {
        return new UserDto.Response(
                user.getUserId(),
                user.getEmail(),
                user.getUsername(),
                user.getBirthYear()
        );
    }
}