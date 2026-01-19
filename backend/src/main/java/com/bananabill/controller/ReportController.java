package com.bananabill.controller;

import com.bananabill.dto.response.ApiResponse;
import com.bananabill.service.ReportService;
import com.bananabill.service.WhatsAppService;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * Report Controller - Analytics and reporting APIs
 */
@RestController
@RequestMapping("/reports")
@Validated
public class ReportController {

    private static final Logger logger = LoggerFactory.getLogger(ReportController.class);

    private final ReportService reportService;
    private final WhatsAppService whatsAppService;

    // Constructor injection
    public ReportController(ReportService reportService, WhatsAppService whatsAppService) {
        this.reportService = reportService;
        this.whatsAppService = whatsAppService;
    }

    /**
     * Get monthly report
     * GET /api/reports/monthly?year=2024&month=12
     */
    @GetMapping("/monthly")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getMonthlyReport(
            @RequestParam @Min(2020) @Max(2100) int year,
            @RequestParam @Min(1) @Max(12) int month) {

        logger.info("Generating monthly report for {}/{}", month, year);

        Map<String, Object> report = reportService.getMonthlyReport(year, month);

        return ResponseEntity.ok(ApiResponse.success(report));
    }

    /**
     * Get list of available months that have bills
     * GET /api/reports/available-months
     */
    @GetMapping("/available-months")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> getAvailableMonths() {
        logger.debug("Fetching available months");

        List<Map<String, Object>> months = reportService.getAvailableMonths();

        return ResponseEntity.ok(ApiResponse.success(months));
    }

    /**
     * Get report for a specific date range
     * GET /api/reports/date-range?startDate=2024-01-01&endDate=2024-01-31
     */
    @GetMapping("/date-range")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getDateRangeReport(
            @RequestParam String startDate,
            @RequestParam String endDate) {

        logger.info("Generating date range report: {} to {}", startDate, endDate);

        LocalDate start = LocalDate.parse(startDate);
        LocalDate end = LocalDate.parse(endDate);

        Map<String, Object> report = reportService.getDateRangeReport(start, end);

        return ResponseEntity.ok(ApiResponse.success(report));
    }

    /**
     * Send statement image to farmer via WhatsApp
     * POST /api/reports/send-statement-whatsapp
     */
    @PostMapping("/send-statement-whatsapp")
    public ResponseEntity<ApiResponse<Map<String, Object>>> sendStatementViaWhatsApp(
            @RequestParam String mobileNumber,
            @RequestParam String farmerName,
            @RequestParam int billCount,
            @RequestParam double totalAmount,
            @RequestParam String imageUrl) {

        logger.info("Sending statement to farmer: {} ({})", farmerName,
                mobileNumber.substring(Math.max(0, mobileNumber.length() - 4)));

        whatsAppService.sendStatementToFarmer(mobileNumber, farmerName, billCount, totalAmount, imageUrl);

        Map<String, Object> result = Map.of(
                "success", true,
                "message", "Statement sent successfully to " + mobileNumber);

        return ResponseEntity.ok(ApiResponse.success("Statement sent", result));
    }
}
