package com.meta.foremeal.global.exception;

import com.meta.foremeal.user.exception.DuplicateEmailException;
import com.meta.foremeal.user.exception.InvalidLoginException;
import com.meta.foremeal.user.exception.UserNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<?> handleUserNotFound(UserNotFoundException e) {
        return ResponseEntity.status(404).body(e.getMessage());
    }

    @ExceptionHandler(DuplicateEmailException.class)
    public ResponseEntity<?> handleDuplicate(DuplicateEmailException e) {
        return ResponseEntity.status(409).body(e.getMessage());
    }

    @ExceptionHandler(InvalidLoginException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public Map<String, Object> handleInvalidLogin(InvalidLoginException e) {
        return Map.of(
                "success", false,
                "code", "INVALID_LOGIN",
                "message", e.getMessage()
        );
    }
}