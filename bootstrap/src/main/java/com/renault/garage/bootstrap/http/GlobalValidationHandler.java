package com.renault.garage.bootstrap.http;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.bind.support.WebExchangeBindException;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalValidationHandler {

    @ExceptionHandler(WebExchangeBindException.class)
    public ResponseEntity<Object> handleWebExchangeBindException(WebExchangeBindException ex) {
        List<Map<String, String>> fieldErrors = ex.getFieldErrors().stream()
                .map(fe -> Map.of("field", fe.getField(), "message", fe.getDefaultMessage()))
                .collect(Collectors.toList());

        Map<String, Object> body = Map.of(
                "error", "Validation failed",
                "details", fieldErrors
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<Object> handleConstraintViolation(ConstraintViolationException ex) {
        List<Map<String, String>> violations = ex.getConstraintViolations().stream()
                .map(this::toMap)
                .collect(Collectors.toList());

        Map<String, Object> body = Map.of(
                "error", "Constraint violation",
                "details", violations
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
    }

    private Map<String, String> toMap(ConstraintViolation<?> v) {
        return Map.of(
                "param", v.getPropertyPath() != null ? v.getPropertyPath().toString() : "",
                "message", v.getMessage()
        );
    }
}
