package com.xpro.rentalmain.rentalmain.advice;

import jakarta.persistence.EntityNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.Builder;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingRequestHeaderException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.stream.Collectors;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    // 1. Data Conflicts (e.g., Duplicate Email)
    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<ApiErrorResponse> handleConflict(ResponseStatusException ex, HttpServletRequest request) {
        return buildResponse(
                ex.getStatusCode(),
                "An account conflict occurred.",
                String.format("The value provided is invalid: %s", ex.getReason()),
                request
        );
    }

    // 2. Resource Not Found
    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<ApiErrorResponse> handleNotFound(EntityNotFoundException ex, HttpServletRequest request) {
        return buildResponse(
                HttpStatus.NOT_FOUND,
                "The requested resource was not found.",
                String.format("Resource lookup failed: %s", ex.getMessage()),
                request
        );
    }

    // 3. Validation/Malformed Request (e.g., missing fields or bad JSON)
    @ExceptionHandler({MethodArgumentNotValidException.class, HttpMessageNotReadableException.class})
    public ResponseEntity<ApiErrorResponse> handleBadRequest(Exception ex, HttpServletRequest request) {
        String detail;
        if (ex instanceof MethodArgumentNotValidException target) {
            detail = target.getBindingResult().getFieldErrors().stream()
                    .map(error -> String.format("%s: %s", error.getField(), error.getDefaultMessage()))
                    .collect(Collectors.joining(", "));
        } else {
            detail = "The request body is malformed or contains invalid data types.";
        }

        return buildResponse(
                HttpStatus.BAD_REQUEST,
                "We couldn't process your request.",
                String.format("Validation details: %s", detail),
                request
        );
    }

    // 4. Access Denied (Security)
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiErrorResponse> handleAccessDenied(AccessDeniedException ex, HttpServletRequest request) {
        return buildResponse(
                HttpStatus.FORBIDDEN,
                "Access Restricted.",
                "You do not have the required permissions to perform this action.",
                request
        );
    }

    // 5. Missing Headers (e.g., X-Rental-App)
    @ExceptionHandler(MissingRequestHeaderException.class)
    public ResponseEntity<ApiErrorResponse> handleMissingHeader(MissingRequestHeaderException ex, HttpServletRequest request) {
        return buildResponse(
                HttpStatus.BAD_REQUEST,
                "Context Information Missing.",
                String.format("The required header '%s' must be provided to continue.", ex.getHeaderName()),
                request
        );
    }

    // 6. Generic Runtime/Unexpected Errors
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ApiErrorResponse> handleRuntime(RuntimeException ex, HttpServletRequest request) {
        log.error("Unhandled Runtime Exception at {}: ", request.getRequestURI(), ex);
        return buildResponse(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "A system error occurred.",
                "Please try again later or contact support if the issue persists.",
                request
        );
    }

    /**
     * Standard Builder for the Response Entity
     */
    private ResponseEntity<ApiErrorResponse> buildResponse(HttpStatusCode status, String message, String details, HttpServletRequest request) {
        ApiErrorResponse error = ApiErrorResponse.builder()
                .message(message)
                .details(details)
                .status(status.value())
                .path(request.getRequestURI())
                .timestamp(LocalDateTime.now())
                .build();
        return new ResponseEntity<>(error, status);
    }

    @Data
    @Builder
    public class ApiErrorResponse {
        private String message;     // High-level human-readable message
        private String details;     // Specific details with populated placeholders
        private int status;
        private String path;
        private LocalDateTime timestamp;
    }
}