package com.meta.foremeal.user.api;

import com.meta.foremeal.user.api.dto.AuthDto;
import com.meta.foremeal.user.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    public AuthDto.LoginResponse login(@RequestBody @Valid AuthDto.LoginRequest request) {
        return authService.login(request);
    }
}
