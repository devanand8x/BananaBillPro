package com.bananabill.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Global Exception Handler for consistent API error responses
 * Handles all exceptions across the application
 * 
 * Exception handling hierarchy:
 * 1. Custom exceptions (BillException, PaymentException, etc.)
 * 2. Spring exceptions (MethodArgumentNotValidException, etc.)
 * 3. Database exceptions (DataAccessException,
 * OptimisticLockingFailureException)
 * 4. Catch-all for RuntimeException and Exception
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    /**
     * Standard error response structure
     */
    public record ErrorResponse(
            int status,
            String error,
            String message,
            String path,
            LocalDateTime timestamp) {
        public ErrorResponse(HttpStatus status, String message, String path) {
            this(status.value(), status.getReasonPhrase(), message, path, LocalDateTime.now());
        }
    }

    /**
     * Handle Resource Not Found (404)
     */
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleResourceNotFound(ResourceNotFoundException ex) {
        logger.warn("Resource not found: {}", ex.getMessage());

        ErrorResponse error = new ErrorResponse(
                HttpStatus.NOT_FOUND.value(),
                "Not Found",
                ex.getMessage(),
                null,
                LocalDateTime.now());

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }

    /**
     * Handle Validation Errors (400)
     */
    @ExceptionHandler(ValidationException.class)
    public ResponseEntity<ErrorResponse> handleValidation(ValidationException ex) {
        logger.warn("Validation error: {}", ex.getMessage());

        ErrorResponse error = new ErrorResponse(
                HttpStatus.BAD_REQUEST.value(),
                "Validation Error",
                ex.getMessage(),
                null,
                LocalDateTime.now());

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    /**
     * Handle Business Logic Errors (422)
     */
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ErrorResponse> handleBusiness(BusinessException ex) {
        logger.warn("Business error: {} - {}", ex.getErrorCode(), ex.getMessage());

        ErrorResponse error = new ErrorResponse(
                HttpStatus.UNPROCESSABLE_ENTITY.value(),
                ex.getErrorCode(),
                ex.getMessage(),
                null,
                LocalDateTime.now());

        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(error);
    }

    /**
     * Handle Payment Errors (400)
     */
    @ExceptionHandler(PaymentException.class)
    public ResponseEntity<ErrorResponse> handlePayment(PaymentException ex) {
        logger.warn("Payment error: {} - {}", ex.getErrorCode(), ex.getMessage());

        ErrorResponse error = new ErrorResponse(
                HttpStatus.BAD_REQUEST.value(),
                ex.getErrorCode(),
                ex.getMessage(),
                null,
                LocalDateTime.now());

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    /**
     * Handle Bill Errors (400/404)
     */
    @ExceptionHandler(BillException.class)
    public ResponseEntity<ErrorResponse> handleBill(BillException ex) {
        logger.warn("Bill error: {} - {}", ex.getErrorCode(), ex.getMessage());

        HttpStatus status = ex.getErrorCode().equals("NOT_FOUND") ||
                ex.getErrorCode().equals("FARMER_NOT_FOUND")
                        ? HttpStatus.NOT_FOUND
                        : HttpStatus.BAD_REQUEST;

        ErrorResponse error = new ErrorResponse(
                status.value(),
                ex.getErrorCode(),
                ex.getMessage(),
                null,
                LocalDateTime.now());

        return ResponseEntity.status(status).body(error);
    }

    /**
     * Handle Optimistic Locking Failures (409 Conflict)
     * Occurs when concurrent updates clash due to @Version annotation
     */
    @ExceptionHandler(org.springframework.dao.OptimisticLockingFailureException.class)
    public ResponseEntity<ErrorResponse> handleOptimisticLocking(
            org.springframework.dao.OptimisticLockingFailureException ex) {
        logger.warn("Concurrent update conflict: {}", ex.getMessage());

        ErrorResponse error = new ErrorResponse(
                HttpStatus.CONFLICT.value(),
                "CONCURRENT_UPDATE",
                "This record was modified by another user. Please refresh and try again.",
                null,
                LocalDateTime.now());

        return ResponseEntity.status(HttpStatus.CONFLICT).body(error);
    }

    /**
     * Handle @Valid annotation errors (400)
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleMethodArgumentNotValid(
            MethodArgumentNotValidException ex) {

        Map<String, String> fieldErrors = new HashMap<>();
        for (FieldError fieldError : ex.getBindingResult().getFieldErrors()) {
            fieldErrors.put(fieldError.getField(), fieldError.getDefaultMessage());
        }

        Map<String, Object> response = new HashMap<>();
        response.put("status", HttpStatus.BAD_REQUEST.value());
        response.put("error", "Validation Failed");
        response.put("message", "Invalid request data");
        response.put("errors", fieldErrors);
        response.put("timestamp", LocalDateTime.now());

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    /**
     * Handle Runtime Exceptions (500)
     */
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ErrorResponse> handleRuntime(RuntimeException ex) {
        logger.error("Runtime error: {}", ex.getMessage(), ex);

        ErrorResponse error = new ErrorResponse(
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                "Internal Server Error",
                "An unexpected error occurred. Please try again later.",
                null,
                LocalDateTime.now());

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }

    /**
     * Catch-all for any unhandled exceptions
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleAll(Exception ex) {
        logger.error("Unhandled exception: {}", ex.getMessage(), ex);

        ErrorResponse error = new ErrorResponse(
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                "Server Error",
                "Something went wrong. Our team has been notified.",
                null,
                LocalDateTime.now());

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }
}
