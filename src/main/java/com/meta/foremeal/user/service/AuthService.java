package com.meta.foremeal.user.service;

import com.meta.foremeal.global.security.jwt.JwtTokenProvider;
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
    private final JwtTokenProvider jwtTokenProvider;

    public AuthService(UserRepository userRepository,
                       PasswordEncoder passwordEncoder,
                       JwtTokenProvider jwtTokenProvider) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtTokenProvider = jwtTokenProvider;
    }

    public AuthDto.LoginResponse login(AuthDto.LoginRequest request) {
        User user = userRepository.findByEmail(request.email())
                .orElseThrow(InvalidLoginException::new);

        if (!passwordEncoder.matches(request.password(), user.getPassword())) {
            throw new InvalidLoginException();
        }

        String accessToken = jwtTokenProvider.generateAccessToken(
                user.getUserId(),
                user.getEmail(),
                user.getRole().name()
        );

        return new AuthDto.LoginResponse(
                accessToken,
                "Bearer",
                jwtTokenProvider.getAccessTokenExpirationMs(),
                user.getUserId(),
                user.getEmail(),
                user.getUsername(),
                user.getRole().name()
        );
    }
}