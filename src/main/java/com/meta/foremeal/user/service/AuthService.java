package com.meta.foremeal.user.service;

import com.meta.foremeal.user.domain.User;
import com.meta.foremeal.user.dto.AuthDto;
import com.meta.foremeal.user.exception.InvalidLoginException;
import com.meta.foremeal.user.repo.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public AuthDto.LoginResponse login(AuthDto.LoginRequest request) {
        User user = userRepository.findByEmail(request.email())
                .orElseThrow(InvalidLoginException::new);

        if (!passwordEncoder.matches(request.password(), user.getPassword())) {
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