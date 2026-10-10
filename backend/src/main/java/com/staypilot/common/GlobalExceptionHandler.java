package com.staypilot.common;

import jakarta.validation.ConstraintViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.List;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiErrorResponse> handleValidation(MethodArgumentNotValidException ex) {
        List<ApiErrorResponse.FieldError> fieldErrors = ex.getBindingResult()
                .getFieldErrors().stream()
                .map(fe -> ApiErrorResponse.FieldError.builder()
                        .field(fe.getField())
                        .message(fe.getDefaultMessage())
                        .build())
                .toList();

        ApiErrorResponse response = ApiErrorResponse.of(
                HttpStatus.BAD_REQUEST.value(),
                "VALIDATION_ERROR",
                "Validation failed"
        );
        response.setFieldErrors(fieldErrors);
        return ResponseEntity.badRequest().body(response);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApiErrorResponse> handleConstraintViolation(ConstraintViolationException ex) {
        return ResponseEntity.badRequest().body(
                ApiErrorResponse.of(HttpStatus.BAD_REQUEST.value(), "VALIDATION_ERROR", ex.getMessage())
        );
    }

    @ExceptionHandler(Exceptions.ResourceNotFoundException.class)
    public ResponseEntity<ApiErrorResponse> handleNotFound(Exceptions.ResourceNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                ApiErrorResponse.of(HttpStatus.NOT_FOUND.value(), "NOT_FOUND", ex.getMessage())
        );
    }

    @ExceptionHandler(Exceptions.ConflictException.class)
    public ResponseEntity<ApiErrorResponse> handleConflict(Exceptions.ConflictException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(
                ApiErrorResponse.of(HttpStatus.CONFLICT.value(), "CONFLICT", ex.getMessage())
        );
    }

    @ExceptionHandler(Exceptions.BadRequestException.class)
    public ResponseEntity<ApiErrorResponse> handleBadRequest(Exceptions.BadRequestException ex) {
        return ResponseEntity.badRequest().body(
                ApiErrorResponse.of(HttpStatus.BAD_REQUEST.value(), "BAD_REQUEST", ex.getMessage())
        );
    }

    @ExceptionHandler(Exceptions.ForbiddenException.class)
    public ResponseEntity<ApiErrorResponse> handleForbidden(Exceptions.ForbiddenException ex) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(
                ApiErrorResponse.of(HttpStatus.FORBIDDEN.value(), "FORBIDDEN", ex.getMessage())
        );
    }

    @ExceptionHandler(Exceptions.UnauthorizedException.class)
    public ResponseEntity<ApiErrorResponse> handleUnauthorized(Exceptions.UnauthorizedException ex) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
                ApiErrorResponse.of(HttpStatus.UNAUTHORIZED.value(), "UNAUTHORIZED", ex.getMessage())
        );
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ApiErrorResponse> handleBadCredentials(BadCredentialsException ex) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
                ApiErrorResponse.of(HttpStatus.UNAUTHORIZED.value(), "INVALID_CREDENTIALS", "Invalid email or password")
        );
    }

    @ExceptionHandler(Exceptions.BusinessRuleException.class)
    public ResponseEntity<ApiErrorResponse> handleBusinessRule(Exceptions.BusinessRuleException ex) {
        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(
                ApiErrorResponse.of(HttpStatus.UNPROCESSABLE_ENTITY.value(), "BUSINESS_RULE_VIOLATION", ex.getMessage())
        );
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiErrorResponse> handleGeneral(Exception ex) {
        // Log the actual exception for debugging but don't expose internals
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                ApiErrorResponse.of(HttpStatus.INTERNAL_SERVER_ERROR.value(), "INTERNAL_ERROR", "An unexpected error occurred")
        );
    }
}
