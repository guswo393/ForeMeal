package com.meta.foremeal.user.service;

import com.meta.foremeal.user.api.dto.AuthDto;
import com.meta.foremeal.user.domain.User;
import com.meta.foremeal.user.exception.InvalidLoginException;
import com.meta.foremeal.user.repo.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class AuthService {

    private final UserRepository userRepository;

    public AuthService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public AuthDto.LoginResponse login(AuthDto.LoginRequest request) {
        User user = userRepository.findByEmail(request.email())
                .orElseThrow(InvalidLoginException::new);

        if (!user.getPassword().equals(request.password())) {
            throw new InvalidLoginException();
        }

        return new AuthDto.LoginResponse(
                "temp-token",
                "Bearer",
                user.getUserId(),
                user.getEmail(),
                user.getUsername(),
                user.getRole().name()
        );
    }
}