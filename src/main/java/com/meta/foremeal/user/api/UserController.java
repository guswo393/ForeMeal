package com.meta.foremeal.user.api;

import com.meta.foremeal.user.dto.UserDto;
import com.meta.foremeal.user.service.UserService;
import jakarta.validation.Valid;
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

    @GetMapping("/{userId}")
    public UserDto.Response getById(@PathVariable Long userId) {
        return userService.getById(userId);
    }

    @PutMapping("/{userId}")
    public UserDto.Response update(@PathVariable Long userId,
                                   @RequestBody @Valid UserDto.UpdateRequest request) {
        return userService.update(userId, request);
    }

    @PatchMapping("/{userId}/password")
    public void changePassword(@PathVariable Long userId,
                               @RequestBody @Valid UserDto.ChangePasswordRequest request) {
        userService.changePassword(userId, request);
    }
}