package com.bananabill.dto.response;

import java.time.LocalDateTime;

/**
 * Standard API Response wrapper for consistent responses
 * 
 * @param <T> The type of data being returned
 */
public record ApiResponse<T>(
        boolean success,
        String message,
        T data,
        LocalDateTime timestamp) {
    /**
     * Success response with data
     */
    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>(true, "Success", data, LocalDateTime.now());
    }

    /**
     * Success response with custom message and data
     */
    public static <T> ApiResponse<T> success(String message, T data) {
        return new ApiResponse<>(true, message, data, LocalDateTime.now());
    }

    /**
     * Success response with message only (no data)
     */
    public static <T> ApiResponse<T> success(String message) {
        return new ApiResponse<>(true, message, null, LocalDateTime.now());
    }

    /**
     * Error response
     */
    public static <T> ApiResponse<T> error(String message) {
        return new ApiResponse<>(false, message, null, LocalDateTime.now());
    }
}
