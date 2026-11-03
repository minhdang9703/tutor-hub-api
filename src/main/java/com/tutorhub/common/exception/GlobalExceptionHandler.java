package com.tutorhub.common.exception;

import jakarta.validation.ConstraintViolationException;
import java.time.Instant;
import java.util.Map;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(ApiException.class) ResponseEntity<ApiError> api(ApiException e) { return ResponseEntity.status(e.getStatus()).body(error(e.getCode(), e.getMessage(), e.getDetails())); }
    @ExceptionHandler({MethodArgumentNotValidException.class, ConstraintViolationException.class}) ResponseEntity<ApiError> validation(Exception e) { return ResponseEntity.badRequest().body(error("VALIDATION_ERROR", "Invalid request", Map.of())); }
    @ExceptionHandler(AccessDeniedException.class) ResponseEntity<ApiError> denied() { return ResponseEntity.status(403).body(error("FORBIDDEN", "Access denied", Map.of())); }
    @ExceptionHandler(Exception.class) ResponseEntity<ApiError> unknown(Exception e) { return ResponseEntity.internalServerError().body(error("INTERNAL_ERROR", "Unexpected error", Map.of())); }
    private ApiError error(String code, String message, Map<String, Object> details) { return new ApiError(code, message, details, Instant.now()); }
}
