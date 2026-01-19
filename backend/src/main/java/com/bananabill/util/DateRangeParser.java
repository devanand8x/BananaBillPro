package com.bananabill.util;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;

/**
 * Date Range Parser - Utility for parsing date range parameters
 * 
 * Centralizes date parsing logic to eliminate duplication across controllers.
 * Handles null, empty, and invalid date strings gracefully.
 */
public final class DateRangeParser {

    private DateRangeParser() {
        // Utility class - no instantiation
    }

    /**
     * Parse a date string and return start of day
     * 
     * @param dateStr Date string in yyyy-MM-dd format (nullable)
     * @return LocalDateTime at start of day, or null if input is null/empty
     * @throws DateTimeParseException if format is invalid
     */
    public static LocalDateTime parseStartDate(String dateStr) {
        if (dateStr == null || dateStr.trim().isEmpty()) {
            return null;
        }
        return LocalDate.parse(dateStr.trim()).atStartOfDay();
    }

    /**
     * Parse a date string and return end of day
     * Uses LocalTime.MAX for precision (23:59:59.999999999)
     * 
     * @param dateStr Date string in yyyy-MM-dd format (nullable)
     * @return LocalDateTime at end of day, or null if input is null/empty
     * @throws DateTimeParseException if format is invalid
     */
    public static LocalDateTime parseEndDate(String dateStr) {
        if (dateStr == null || dateStr.trim().isEmpty()) {
            return null;
        }
        return LocalDate.parse(dateStr.trim()).atTime(LocalTime.MAX);
    }

    /**
     * Parse start and end date strings into a DateRange record
     * 
     * @param startDateStr Start date string (nullable)
     * @param endDateStr   End date string (nullable)
     * @return DateRange with parsed values (may contain nulls)
     */
    public static DateRange parse(String startDateStr, String endDateStr) {
        return new DateRange(
                parseStartDate(startDateStr),
                parseEndDate(endDateStr));
    }

    /**
     * Check if a date range is effectively empty (both null)
     */
    public static boolean isEmpty(DateRange range) {
        return range.startDate() == null && range.endDate() == null;
    }

    /**
     * Immutable date range container
     */
    public record DateRange(LocalDateTime startDate, LocalDateTime endDate) {

        public boolean hasStartDate() {
            return startDate != null;
        }

        public boolean hasEndDate() {
            return endDate != null;
        }

        public boolean isEmpty() {
            return startDate == null && endDate == null;
        }
    }
}
