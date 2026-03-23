package com.meta.foremeal.user.service;

import com.meta.foremeal.user.domain.User;
import com.meta.foremeal.user.dto.UserDto;
import com.meta.foremeal.user.exception.DuplicateEmailException;
import com.meta.foremeal.user.exception.InvalidLoginException;
import com.meta.foremeal.user.exception.UserNotFoundException;
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
            throw new DuplicateEmailException(request.email());
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

    @Transactional(readOnly = true)
    public UserDto.LoginResponse login(UserDto.LoginRequest request) {
        User user = userRepository.findByEmail(request.email())
                .orElseThrow(InvalidLoginException::new);

        if (!user.getPassword().equals(request.password())) {
            throw new InvalidLoginException();
        }

        return new UserDto.LoginResponse(
                user.getUserId(),
                user.getEmail(),
                user.getUsername()
        );
    }

    private User findUser(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));
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