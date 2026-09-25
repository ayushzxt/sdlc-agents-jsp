package com.example.leavemanagement.controller;

import com.example.leavemanagement.dto.ApiError;
import com.example.leavemanagement.dto.FieldErrorResponse;
import com.example.leavemanagement.exception.*;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

@RestControllerAdvice
public class ApiExceptionHandler {
    @ExceptionHandler(MethodArgumentNotValidException.class)
    ResponseEntity<ApiError> validation(MethodArgumentNotValidException exception) { return error(HttpStatus.BAD_REQUEST, "VALIDATION_ERROR", "Validation failed", exception.getBindingResult().getFieldErrors().stream().map(e -> new FieldErrorResponse(e.getField(), e.getDefaultMessage())).toList()); }
    @ExceptionHandler({BadRequestException.class, MethodArgumentTypeMismatchException.class})
    ResponseEntity<ApiError> badRequest(Exception exception) { return error(HttpStatus.BAD_REQUEST, "BAD_REQUEST", exception.getMessage(), List.of()); }
    @ExceptionHandler(ConflictException.class) ResponseEntity<ApiError> conflict(ConflictException e) { return error(HttpStatus.CONFLICT, "CONFLICT", e.getMessage(), List.of()); }
    @ExceptionHandler({ObjectOptimisticLockingFailureException.class, jakarta.persistence.OptimisticLockException.class})
    ResponseEntity<ApiError> optimisticConflict() { return error(HttpStatus.CONFLICT, "CONFLICT", "Request state changed; please refresh and try again", List.of()); }
    @ExceptionHandler(ForbiddenException.class) ResponseEntity<ApiError> forbidden(ForbiddenException e) { return error(HttpStatus.FORBIDDEN, "FORBIDDEN", e.getMessage(), List.of()); }
    @ExceptionHandler(ResourceNotFoundException.class) ResponseEntity<ApiError> notFound(ResourceNotFoundException e) { return error(HttpStatus.NOT_FOUND, "NOT_FOUND", e.getMessage(), List.of()); }
    @ExceptionHandler(BadCredentialsException.class) ResponseEntity<ApiError> credentials() { return error(HttpStatus.UNAUTHORIZED, "UNAUTHORIZED", "Invalid credentials", List.of()); }
    @ExceptionHandler(LoginThrottledException.class) ResponseEntity<ApiError> throttled() { return error(HttpStatus.TOO_MANY_REQUESTS, "TOO_MANY_REQUESTS", "Too many login attempts", List.of()); }
    private ResponseEntity<ApiError> error(HttpStatus status, String code, String message, List<FieldErrorResponse> fields) { return ResponseEntity.status(status).body(new ApiError(code, message == null ? status.getReasonPhrase() : message, fields)); }
}