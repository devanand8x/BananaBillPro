package com.bananabill.controller;

import com.bananabill.dto.CreateBillRequest;
import com.bananabill.dto.response.ApiResponse;
import com.bananabill.dto.response.BillResponse;
import com.bananabill.model.Bill;
import com.bananabill.service.BillService;
import com.bananabill.service.WhatsAppService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import com.bananabill.util.DateRangeParser;

/**
 * Bill Controller - REST API for bill management
 * Clean architecture with proper error handling via GlobalExceptionHandler
 */
@RestController
@RequestMapping("/bills")
@Validated
public class BillController {

    private static final Logger logger = LoggerFactory.getLogger(BillController.class);

    private final BillService billService;
    private final WhatsAppService whatsAppService;

    // Constructor injection (best practice)
    public BillController(BillService billService, WhatsAppService whatsAppService) {
        this.billService = billService;
        this.whatsAppService = whatsAppService;
    }

    // ===================== CRUD OPERATIONS =====================

    /**
     * Create new bill
     * POST /api/bills
     */
    @PostMapping
    public ResponseEntity<ApiResponse<BillResponse>> createBill(@Valid @RequestBody CreateBillRequest request) {
        logger.info("Creating bill for farmer: {}", request.getFarmerId());

        Bill bill = billService.createBill(request);

        logger.info("Bill created: {}", bill.getBillNumber());
        return ResponseEntity.ok(ApiResponse.success("Bill created successfully", BillResponse.from(bill)));
    }

    /**
     * Get bill by ID
     * GET /api/bills/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<BillResponse>> getBillById(@PathVariable String id) {
        logger.debug("Fetching bill by ID: {}", id);

        Bill bill = billService.getBillById(id);

        return ResponseEntity.ok(ApiResponse.success(BillResponse.from(bill)));
    }

    /**
     * Get bill by bill number
     * GET /api/bills/number/{billNumber}
     */
    @GetMapping("/number/{billNumber}")
    public ResponseEntity<ApiResponse<BillResponse>> getBillByNumber(@PathVariable String billNumber) {
        logger.debug("Fetching bill by number: {}", billNumber);

        Bill bill = billService.getBillByNumber(billNumber);

        return ResponseEntity.ok(ApiResponse.success(BillResponse.from(bill)));
    }

    /**
     * Update bill
     * PUT /api/bills/{id}
     */
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<BillResponse>> updateBill(
            @PathVariable String id,
            @Valid @RequestBody CreateBillRequest request) {
        logger.info("Updating bill: {}", id);

        Bill updatedBill = billService.updateBill(id, request);

        logger.info("Bill updated: {}", updatedBill.getBillNumber());
        return ResponseEntity.ok(ApiResponse.success("Bill updated successfully", BillResponse.from(updatedBill)));
    }

    /**
     * Delete bill
     * DELETE /api/bills/{id}
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteBill(@PathVariable String id) {
        logger.info("Deleting bill: {}", id);

        billService.deleteBill(id);

        return ResponseEntity.ok(ApiResponse.success("Bill deleted successfully"));
    }

    // ===================== QUERY OPERATIONS =====================

    /**
     * Get bills by farmer mobile
     * GET /api/bills/farmer/{mobile}
     */
    @GetMapping("/farmer/{mobile}")
    public ResponseEntity<ApiResponse<List<BillResponse>>> getBillsByFarmerMobile(@PathVariable String mobile) {
        logger.debug("Fetching bills for farmer mobile: ******{}", mobile.substring(Math.max(0, mobile.length() - 4)));

        List<Bill> bills = billService.getBillsByFarmerMobile(mobile);
        List<BillResponse> responses = bills.stream().map(BillResponse::from).toList();

        return ResponseEntity.ok(ApiResponse.success(responses));
    }

    /**
     * Get recent bills
     * GET /api/bills/recent?limit=10
     */
    @GetMapping("/recent")
    public ResponseEntity<ApiResponse<List<BillResponse>>> getRecentBills(
            @RequestParam(defaultValue = "10") @Min(1) int limit) {
        logger.debug("Fetching {} recent bills", limit);

        List<Bill> bills = billService.getRecentBills(limit);
        List<BillResponse> responses = bills.stream().map(BillResponse::from).toList();

        return ResponseEntity.ok(ApiResponse.success(responses));
    }

    /**
     * Filter bills by date range
     * GET /api/bills/filter?startDate=2024-01-01&endDate=2024-01-31
     */
    @GetMapping("/filter")
    public ResponseEntity<ApiResponse<Map<String, Object>>> filterBillsByDateRange(
            @RequestParam String startDate,
            @RequestParam String endDate) {
        logger.debug("Filtering bills: {} to {}", startDate, endDate);

        Map<String, Object> result = billService.getBillsByDateRange(startDate, endDate);

        return ResponseEntity.ok(ApiResponse.success(result));
    }

    // ===================== STATISTICS =====================

    /**
     * Get today's stats
     * GET /api/bills/stats/today
     */
    @GetMapping("/stats/today")
    public ResponseEntity<ApiResponse<Map<String, Long>>> getTodayStats() {
        Long count = billService.getTodayBillsCount();

        return ResponseEntity.ok(ApiResponse.success(Map.of("count", count)));
    }

    /**
     * Get total stats
     * GET /api/bills/stats/total
     */
    @GetMapping("/stats/total")
    public ResponseEntity<ApiResponse<Map<String, Long>>> getTotalStats() {
        Long count = billService.getTotalBillsCount();

        return ResponseEntity.ok(ApiResponse.success(Map.of("count", count)));
    }

    /**
     * Get unpaid stats
     * GET /api/bills/stats/unpaid
     */
    @GetMapping("/stats/unpaid")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getUnpaidStats() {
        Map<String, Object> stats = Map.of(
                "count", billService.getUnpaidCount(),
                "totalAmount", billService.getTotalUnpaidAmount());

        return ResponseEntity.ok(ApiResponse.success(stats));
    }

    // ===================== PAYMENT OPERATIONS =====================

    /**
     * Mark bill as paid
     * POST /api/bills/{id}/mark-paid
     */
    @PostMapping("/{id}/mark-paid")
    public ResponseEntity<ApiResponse<BillResponse>> markAsPaid(@PathVariable String id) {
        logger.info("Marking bill as paid: {}", id);

        Bill bill = billService.markAsPaid(id);

        return ResponseEntity.ok(ApiResponse.success("Bill marked as paid", BillResponse.from(bill)));
    }

    /**
     * Record partial payment
     * POST /api/bills/{id}/record-payment?amount=5000
     */
    @PostMapping("/{id}/record-payment")
    public ResponseEntity<ApiResponse<BillResponse>> recordPayment(
            @PathVariable String id,
            @RequestParam BigDecimal amount) {
        logger.info("Recording payment for bill {}: {}", id, amount);

        Bill bill = billService.recordPayment(id, amount);

        return ResponseEntity.ok(ApiResponse.success("Payment recorded", BillResponse.from(bill)));
    }

    /**
     * Get unpaid bills
     * GET /api/bills/unpaid
     */
    @GetMapping("/unpaid")
    public ResponseEntity<ApiResponse<List<BillResponse>>> getUnpaidBills() {
        List<Bill> bills = billService.getUnpaidBills();
        List<BillResponse> responses = bills.stream().map(BillResponse::from).toList();

        return ResponseEntity.ok(ApiResponse.success(responses));
    }

    /**
     * Get overdue bills
     * GET /api/bills/overdue
     */
    @GetMapping("/overdue")
    public ResponseEntity<ApiResponse<List<BillResponse>>> getOverdueBills() {
        List<Bill> bills = billService.getOverdueBills();
        List<BillResponse> responses = bills.stream().map(BillResponse::from).toList();

        return ResponseEntity.ok(ApiResponse.success(responses));
    }

    /**
     * Set due date for bill
     * POST /api/bills/{id}/set-due-date?dueDate=2024-12-31T23:59:59
     */
    @PostMapping("/{id}/set-due-date")
    public ResponseEntity<ApiResponse<BillResponse>> setDueDate(
            @PathVariable String id,
            @RequestParam String dueDate) {
        logger.info("Setting due date for bill {}: {}", id, dueDate);

        LocalDateTime due = LocalDateTime.parse(dueDate);
        Bill bill = billService.setDueDate(id, due);

        return ResponseEntity.ok(ApiResponse.success("Due date set", BillResponse.from(bill)));
    }

    /**
     * Send bill to WhatsApp
     * POST /api/bills/{id}/send-whatsapp
     * Body: { "imageUrl": "data:image/png;base64,..." }
     */
    @PostMapping("/{id}/send-whatsapp")
    public ResponseEntity<ApiResponse<Map<String, String>>> sendBillToWhatsApp(
            @PathVariable String id,
            @RequestBody Map<String, String> body) {
        logger.info("Sending bill {} to WhatsApp", id);

        String imageUrl = body.get("imageUrl");
        logger.info("=== BILL WHATSAPP DEBUG ===");
        logger.info("ImageUrl null? {}", imageUrl == null);
        logger.info("ImageUrl length: {}", imageUrl != null ? imageUrl.length() : 0);
        logger.info("ImageUrl starts with 'data:'? {}", imageUrl != null && imageUrl.startsWith("data:"));
        logger.info("First 50 chars: {}",
                imageUrl != null && imageUrl.length() > 50 ? imageUrl.substring(0, 50) : imageUrl);

        Bill bill = billService.getBillById(id);
        whatsAppService.sendBillToFarmer(bill, imageUrl);

        Map<String, String> result = Map.of(
                "message", "Bill sent to WhatsApp successfully!",
                "phone", bill.getFarmer().getMobileNumber());

        return ResponseEntity.ok(ApiResponse.success("WhatsApp sent", result));
    }

    /**
     * Send payment confirmation via WhatsApp
     * POST /api/bills/{id}/send-confirmation
     */
    @PostMapping("/{id}/send-confirmation")
    public ResponseEntity<ApiResponse<Map<String, Object>>> sendPaymentConfirmation(@PathVariable String id) {
        logger.info("Sending payment confirmation for bill: {}", id);

        Bill bill = billService.getBillById(id);
        whatsAppService.sendPaymentConfirmation(bill);

        Map<String, Object> result = Map.of(
                "success", true,
                "message", "Payment confirmation sent to " + bill.getFarmer().getName(),
                "phone", bill.getFarmer().getMobileNumber());

        return ResponseEntity.ok(ApiResponse.success(result));
    }

    // ===================== REPORTS =====================

    /**
     * Get farmer report
     * GET
     * /bills/farmer-report/{farmerId}?startDate=...&endDate=...&paymentStatus=...
     */
    @GetMapping("/farmer-report/{farmerId}")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getFarmerReport(
            @PathVariable String farmerId,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate,
            @RequestParam(required = false) String paymentStatus) {

        logger.debug("Generating farmer report for: {}", farmerId);

        DateRangeParser.DateRange range = DateRangeParser.parse(startDate, endDate);
        Map<String, Object> report = billService.getFarmerReport(
                farmerId, range.startDate(), range.endDate(), paymentStatus);

        return ResponseEntity.ok(ApiResponse.success(report));
    }

    /**
     * Search bills with filters
     * GET
     * /bills/search-with-filters?mobileNumber=...&startDate=...&endDate=...&paymentStatus=...
     */
    @GetMapping("/search-with-filters")
    public ResponseEntity<ApiResponse<Map<String, Object>>> searchBillsWithFilters(
            @RequestParam(required = false) String mobileNumber,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate,
            @RequestParam(required = false) String paymentStatus) {

        logger.debug("Searching bills with filters");

        DateRangeParser.DateRange range = DateRangeParser.parse(startDate, endDate);
        Map<String, Object> result = billService.searchBillsWithFilters(
                mobileNumber, range.startDate(), range.endDate(), paymentStatus);

        return ResponseEntity.ok(ApiResponse.success(result));
    }
}
