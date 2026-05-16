package com.meta.foremeal.user.exception;

public class UserNotFoundException extends RuntimeException {
    public UserNotFoundException(Long userId) {
        super("사용자 정보를 찾을 수 없습니다.");
    }
}
