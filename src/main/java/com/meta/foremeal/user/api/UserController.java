package com.meta.foremeal.user.api;

import com.meta.foremeal.global.security.principal.CustomUserPrincipal;
import com.meta.foremeal.user.dto.UserDto;
import com.meta.foremeal.user.service.UserService;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping
    public UserDto.Response create(@RequestBody @Valid UserDto.CreateRequest request) {
        return userService.create(request);
    }

    @GetMapping("/me")
    public UserDto.Response getMe(@AuthenticationPrincipal CustomUserPrincipal principal) {
        return userService.getById(principal.getUserId());
    }

    @PutMapping("/me")
    public UserDto.Response updateMe(@AuthenticationPrincipal CustomUserPrincipal principal,
                                     @RequestBody @Valid UserDto.UpdateRequest request) {
        return userService.update(principal.getUserId(), request);
    }

    @PatchMapping("/me/password")
    public void changePassword(@AuthenticationPrincipal CustomUserPrincipal principal,
                               @RequestBody @Valid UserDto.ChangePasswordRequest request) {
        userService.changePassword(principal.getUserId(), request);
    }
}