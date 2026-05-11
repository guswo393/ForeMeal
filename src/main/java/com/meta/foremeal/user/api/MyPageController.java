package com.meta.foremeal.user.api;

import com.meta.foremeal.global.security.principal.CustomUserPrincipal;
import com.meta.foremeal.user.dto.MyPageDto;
import com.meta.foremeal.user.service.MyPageService;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/mypage")
public class MyPageController {

    private final MyPageService myPageService;

    public MyPageController(MyPageService myPageService) {
        this.myPageService = myPageService;
    }

    @GetMapping("/me")
    public MyPageDto.Response getMyPage(@AuthenticationPrincipal CustomUserPrincipal principal) {
        return myPageService.getMyPage(principal.getUserId());
    }

    @PutMapping("/me")
    public MyPageDto.Response updateMyPage(@AuthenticationPrincipal CustomUserPrincipal principal,
                                           @RequestBody @Valid MyPageDto.UpdateRequest request) {
        return myPageService.updateMyPage(principal.getUserId(), request);
    }
}
