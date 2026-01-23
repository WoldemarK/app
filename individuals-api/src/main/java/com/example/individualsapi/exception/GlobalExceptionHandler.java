package com.example.individualsapi.exception;

import com.example.individual.dto.ErrorResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.nio.file.AccessDeniedException;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponse> handleAccessDeniedException(AccessDeniedException e) {
        log.error("Access denied: {}", e.getMessage());
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(new ErrorResponse()
                        .status(401)
                        .error("User not authorized: %s".formatted(e.getMessage())));
    }

    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<ErrorResponse> handleBadRequestException(BadRequestException e) {
        log.error("Bad request: {}", e.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new ErrorResponse()
                        .status(400)
                        .error("Bad request: %s".formatted(e.getMessage())));
    }

    // 500, 409, 400 Ошибки в UserRegistrationService
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleException(Exception e) {
        log.error("Exception: {}", e.getMessage());
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ErrorResponse().status(500)
                        .error("Exception: %s".formatted(e.getMessage())));
    }

    @ExceptionHandler(UserAlreadyExistsException.class)
    public ResponseEntity<ErrorResponse> handleUserAlreadyExistsException(UserAlreadyExistsException e) {
        log.error("UserAlreadyExistsException: {}", e.getMessage());
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(new ErrorResponse().status(409)
                        .error("USER_ALREADY_EXISTS: %s".formatted(e.getMessage())));
    }

    @ExceptionHandler(ValidationError.class)
    public ResponseEntity<ErrorResponse> handleValidationErrorException(ValidationError e) {
        log.error("UserRegistrationRequest validation error: {}", e.getMessage());
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(new ErrorResponse().status(400)
                        .error("USER_ALREADY_EXISTS: %s".formatted(e.getMessage())));
    }
}
