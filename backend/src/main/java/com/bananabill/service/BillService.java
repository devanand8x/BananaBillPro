package com.bananabill.service;

import com.bananabill.dto.CreateBillRequest;
import com.bananabill.exception.BillException;
import com.bananabill.model.Bill;
import com.bananabill.model.Farmer;
import com.bananabill.model.PaymentHistory;
import com.bananabill.model.PaymentStatus;
import com.bananabill.model.User;
import com.bananabill.repository.BillRepository;
import com.bananabill.repository.FarmerRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.bananabill.util.SecurityUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * Bill Service - Orchestrator for bill operations
 * 
 * This service acts as a facade, delegating to specialized services:
 * - BillCalculationService: Weight and amount calculations
 * - PaymentService: Payment recording and status management
 * - BillQueryService: Read-only queries and reports
 * 
 * DOMAIN FORMULA (preserved, not modified):
 * Chargeable Weight = Net Weight + Danda + Tut (ADD, not subtract)
 */
@Service
@Transactional
public class BillService {

    private static final Logger logger = LoggerFactory.getLogger(BillService.class);

    private final BillRepository billRepository;
    private final FarmerRepository farmerRepository;
    private final CounterService counterService;
    private final BillCalculationService calculationService;
    private final PaymentService paymentService;

    public BillService(
            BillRepository billRepository,
            FarmerRepository farmerRepository,
            CounterService counterService,
            BillCalculationService calculationService,
            PaymentService paymentService) {
        this.billRepository = billRepository;
        this.farmerRepository = farmerRepository;
        this.counterService = counterService;
        this.calculationService = calculationService;
        this.paymentService = paymentService;
    }

    // ==================== BILL CREATION ====================

    /**
     * Create a new bill with all calculations performed server-side
     */
    public Bill createBill(CreateBillRequest request) {
        User currentUser = getCurrentUser();

        Farmer farmer = farmerRepository.findById(request.getFarmerId())
                .orElseThrow(() -> BillException.farmerNotFound(request.getFarmerId()));

        String billNumber = counterService.generateBillNumber();

        // Extract and validate inputs
        BigDecimal grossWeight = ensureNotNull(request.getGrossWeight(), "grossWeight");
        BigDecimal pattiWeight = defaultIfNull(request.getPattiWeight(), BigDecimal.ZERO);
        BigDecimal tutWastage = defaultIfNull(request.getTutWastage(), BigDecimal.ZERO);
        BigDecimal ratePerKg = ensureNotNull(request.getRatePerKg(), "ratePerKg");
        BigDecimal majuri = defaultIfNull(request.getMajuri(), BigDecimal.ZERO);
        int boxCount = request.getBoxCount() != null ? request.getBoxCount() : 0;

        // Delegate calculations to BillCalculationService
        BillCalculationService.BillCalculationResult calc = calculationService.calculateBill(
                grossWeight, pattiWeight, boxCount, tutWastage, ratePerKg, majuri);

        // Build bill entity
        Bill bill = buildBill(request, billNumber, calc, farmer, currentUser);

        logger.info("Creating bill {} for farmer {} by user {}",
                billNumber, farmer.getMobileNumber(), currentUser.getMobileNumber());

        return billRepository.save(bill);
    }

    /**
     * Update existing bill
     */
    public Bill updateBill(String id, CreateBillRequest request) {
        Bill existingBill = billRepository.findById(id)
                .orElseThrow(() -> BillException.notFound(id));

        User currentUser = getCurrentUser();
        Farmer farmer = farmerRepository.findById(request.getFarmerId())
                .orElseThrow(() -> BillException.farmerNotFound(request.getFarmerId()));

        // Extract inputs
        BigDecimal grossWeight = ensureNotNull(request.getGrossWeight(), "grossWeight");
        BigDecimal pattiWeight = defaultIfNull(request.getPattiWeight(), BigDecimal.ZERO);
        BigDecimal tutWastage = defaultIfNull(request.getTutWastage(), BigDecimal.ZERO);
        BigDecimal ratePerKg = ensureNotNull(request.getRatePerKg(), "ratePerKg");
        BigDecimal majuri = defaultIfNull(request.getMajuri(), BigDecimal.ZERO);
        int boxCount = request.getBoxCount() != null ? request.getBoxCount() : 0;

        // Recalculate
        BillCalculationService.BillCalculationResult calc = calculationService.calculateBill(
                grossWeight, pattiWeight, boxCount, tutWastage, ratePerKg, majuri);

        // Update bill
        updateBillFields(existingBill, request, calc, farmer, currentUser);

        logger.info("Bill {} updated by user {}", existingBill.getBillNumber(), currentUser.getMobileNumber());

        return billRepository.save(existingBill);
    }

    /**
     * Delete bill
     */
    public void deleteBill(String id) {
        Bill bill = billRepository.findById(id)
                .orElseThrow(() -> BillException.notFound(id));
        logger.warn("Bill {} deleted by user {}", bill.getBillNumber(), getCurrentUser().getMobileNumber());
        billRepository.delete(bill);
    }

    // ==================== PAYMENT OPERATIONS (Delegate to PaymentService)
    // ====================

    public Bill recordPayment(String billId, BigDecimal amount) {
        return paymentService.recordPayment(billId, amount);
    }

    public Bill markAsPaid(String billId) {
        return paymentService.markAsPaid(billId);
    }

    public Bill updatePaymentStatus(String billId, PaymentStatus status, BigDecimal paidAmount) {
        return paymentService.updatePaymentStatus(billId, status, paidAmount);
    }

    public List<PaymentHistory> getPaymentHistory(String billId) {
        return paymentService.getPaymentHistory(billId);
    }

    // ==================== QUERY OPERATIONS (Inlined from BillQueryService)
    // ====================

    public Bill getBillById(String id) {
        return billRepository.findById(id)
                .orElseThrow(() -> BillException.notFound(id));
    }

    public Bill getBillByNumber(String billNumber) {
        return billRepository.findByBillNumber(billNumber)
                .orElseThrow(() -> BillException.notFound(billNumber));
    }

    public List<Bill> getBillsByFarmerMobile(String mobile) {
        Farmer farmer = farmerRepository.findByMobileNumber(mobile)
                .orElseThrow(() -> BillException.farmerNotFound(mobile));
        return billRepository.findByFarmerId(farmer.getId());
    }

    public List<Bill> getUnpaidBills() {
        return billRepository.findUnpaidBills(org.springframework.data.domain.Sort.by(
                org.springframework.data.domain.Sort.Direction.DESC, "createdAt"));
    }

    public List<Bill> getOverdueBills() {
        return billRepository.findByDueDateBeforeAndPaymentStatusNot(
                LocalDateTime.now(), PaymentStatus.PAID);
    }

    public List<Bill> getRecentBills(int limit) {
        return billRepository.findRecentBills(
                org.springframework.data.domain.PageRequest.of(0, limit));
    }

    public Long getTodayBillsCount() {
        return billRepository.countByCreatedAtAfter(LocalDateTime.now().toLocalDate().atStartOfDay());
    }

    public Long getTotalBillsCount() {
        return billRepository.count();
    }

    public Long getUnpaidCount() {
        return billRepository.countUnpaidBills();
    }

    public BigDecimal getTotalUnpaidAmount() {
        // Simplified - sum unpaid amounts in-memory for now
        List<Bill> unpaid = getUnpaidBills();
        return unpaid.stream()
                .map(Bill::getNetAmount)
                .filter(a -> a != null)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public Map<String, Object> getBillsByDateRange(String startDateStr, String endDateStr) {
        LocalDateTime startDate = parseStartDate(startDateStr);
        LocalDateTime endDate = parseEndDate(endDateStr);
        List<Bill> bills = billRepository.findByCreatedAtBetween(startDate, endDate);
        Map<String, Object> result = new java.util.HashMap<>();
        result.put("bills", bills);
        result.put("count", bills.size());
        return result;
    }

    public Map<String, Object> getFarmerReport(String farmerId, LocalDateTime startDate,
            LocalDateTime endDate, String paymentStatus) {
        Map<String, Object> report = new java.util.HashMap<>();
        Farmer farmer = farmerRepository.findById(farmerId).orElse(null);
        report.put("farmer", farmer);

        List<Bill> allBills = billRepository.findByFarmerId(farmerId);
        List<Bill> bills = allBills;

        // Apply date filters
        if (startDate != null) {
            final LocalDateTime start = startDate;
            bills = bills.stream()
                    .filter(bill -> bill.getCreatedAt() != null && !bill.getCreatedAt().isBefore(start))
                    .collect(java.util.stream.Collectors.toList());
        }
        if (endDate != null) {
            final LocalDateTime end = endDate;
            bills = bills.stream()
                    .filter(bill -> bill.getCreatedAt() != null && !bill.getCreatedAt().isAfter(end))
                    .collect(java.util.stream.Collectors.toList());
        }

        // Apply payment status filter
        if (paymentStatus != null && !paymentStatus.isEmpty() && !paymentStatus.equalsIgnoreCase("ALL")) {
            String statusUpper = paymentStatus.toUpperCase();
            bills = bills.stream()
                    .filter(bill -> bill.getPaymentStatus() != null &&
                            bill.getPaymentStatus().name().equals(statusUpper))
                    .collect(java.util.stream.Collectors.toList());
        }

        report.put("bills", bills);
        report.put("totalBills", bills.size());
        report.put("totalBillsUnfiltered", allBills.size());
        report.put("isFiltered", startDate != null || endDate != null ||
                (paymentStatus != null && !paymentStatus.isEmpty() && !paymentStatus.equalsIgnoreCase("ALL")));

        // Calculate totals
        BigDecimal totalAmount = bills.stream()
                .map(Bill::getNetAmount)
                .filter(a -> a != null)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalWeight = bills.stream()
                .map(Bill::getFinalNetWeight)
                .filter(w -> w != null)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        List<Bill> unpaidBillsList = bills.stream()
                .filter(bill -> bill.getPaymentStatus() != PaymentStatus.PAID)
                .collect(java.util.stream.Collectors.toList());

        BigDecimal unpaidAmount = unpaidBillsList.stream()
                .map(Bill::getNetAmount)
                .filter(a -> a != null)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        report.put("totalAmount", totalAmount);
        report.put("totalWeight", totalWeight);
        report.put("unpaidAmount", unpaidAmount);
        report.put("unpaidBills", unpaidBillsList.size());

        return report;
    }

    public Map<String, Object> searchBillsWithFilters(String mobileNumber, LocalDateTime startDate,
            LocalDateTime endDate, String paymentStatus) {
        Map<String, Object> result = new java.util.HashMap<>();
        List<Bill> bills;

        if (mobileNumber != null && !mobileNumber.trim().isEmpty()) {
            Farmer farmer = farmerRepository.findByMobileNumber(mobileNumber).orElse(null);
            if (farmer != null) {
                bills = billRepository.findByFarmerId(farmer.getId());
                result.put("farmer", farmer);
            } else {
                result.put("bills", List.of());
                result.put("count", 0);
                return result;
            }
        } else {
            bills = getRecentBills(100);
        }

        // Apply date filter - start date
        if (startDate != null) {
            final LocalDateTime start = startDate;
            bills = bills.stream()
                    .filter(bill -> bill.getCreatedAt() != null && !bill.getCreatedAt().isBefore(start))
                    .collect(java.util.stream.Collectors.toList());
        }

        // Apply date filter - end date
        if (endDate != null) {
            final LocalDateTime end = endDate;
            bills = bills.stream()
                    .filter(bill -> bill.getCreatedAt() != null && !bill.getCreatedAt().isAfter(end))
                    .collect(java.util.stream.Collectors.toList());
        }

        // Apply payment status filter
        if (paymentStatus != null && !paymentStatus.trim().isEmpty()) {
            String statusUpper = paymentStatus.toUpperCase();
            bills = bills.stream()
                    .filter(bill -> bill.getPaymentStatus() != null &&
                            bill.getPaymentStatus().name().equals(statusUpper))
                    .collect(java.util.stream.Collectors.toList());
        }

        result.put("bills", bills);
        result.put("count", bills.size());
        return result;
    }

    // ==================== ADDITIONAL OPERATIONS ====================

    public Bill setDueDate(String billId, LocalDateTime dueDate) {
        Bill bill = billRepository.findById(billId)
                .orElseThrow(() -> BillException.notFound(billId));
        bill.setDueDate(dueDate);
        bill.setUpdatedAt(LocalDateTime.now());
        return billRepository.save(bill);
    }

    public Bill updateLastReminderSent(String billId) {
        Bill bill = billRepository.findById(billId)
                .orElseThrow(() -> BillException.notFound(billId));
        bill.setLastReminderSent(LocalDateTime.now());
        return billRepository.save(bill);
    }

    // ==================== PRIVATE HELPERS ====================

    private User getCurrentUser() {
        return SecurityUtils.getCurrentUser();
    }

    private BigDecimal ensureNotNull(BigDecimal value, String fieldName) {
        if (value == null) {
            throw BillException.invalidInput(fieldName, "cannot be null");
        }
        return value;
    }

    private BigDecimal defaultIfNull(BigDecimal value, BigDecimal defaultValue) {
        return value != null ? value : defaultValue;
    }

    private LocalDateTime parseStartDate(String dateStr) {
        if (dateStr == null || dateStr.isEmpty())
            return null;
        return java.time.LocalDate.parse(dateStr).atStartOfDay();
    }

    private LocalDateTime parseEndDate(String dateStr) {
        if (dateStr == null || dateStr.isEmpty())
            return null;
        return java.time.LocalDate.parse(dateStr).atTime(java.time.LocalTime.MAX);
    }

    private Bill buildBill(CreateBillRequest request, String billNumber,
            BillCalculationService.BillCalculationResult calc, Farmer farmer, User currentUser) {
        Bill bill = new Bill();
        bill.setBillNumber(billNumber);
        bill.setFarmerId(request.getFarmerId());
        bill.setVehicleNumber(request.getVehicleNumber());
        bill.setGrossWeight(calculationService.scaleWeight(request.getGrossWeight()));
        bill.setPattiWeight(calculationService.scaleWeight(defaultIfNull(request.getPattiWeight(), BigDecimal.ZERO)));
        bill.setBoxCount(request.getBoxCount() != null ? request.getBoxCount() : 0);
        bill.setNetWeight(calculationService.scaleWeight(calc.baseNetWeight()));
        bill.setDandaWeight(calculationService.scaleWeight(calc.dandaWeight()));
        bill.setTutWastage(calculationService.scaleWeight(defaultIfNull(request.getTutWastage(), BigDecimal.ZERO)));
        bill.setFinalNetWeight(calculationService.scaleWeight(calc.chargeableWeight()));
        bill.setRatePerKg(calculationService.scaleWeight(request.getRatePerKg()));
        bill.setTotalAmount(calculationService.scaleMoney(calc.totalAmount()));
        bill.setMajuri(calculationService.scaleMoney(defaultIfNull(request.getMajuri(), BigDecimal.ZERO)));
        bill.setNetAmount(calculationService.scaleMoney(calc.netAmount()));
        bill.setCreatedBy(currentUser.getId());
        bill.setFarmer(farmer);
        bill.setPaymentStatus(PaymentStatus.UNPAID);
        bill.setPaidAmount(BigDecimal.ZERO);
        return bill;
    }

    private void updateBillFields(Bill bill, CreateBillRequest request,
            BillCalculationService.BillCalculationResult calc, Farmer farmer, User currentUser) {
        bill.setFarmerId(request.getFarmerId());
        bill.setVehicleNumber(request.getVehicleNumber());
        bill.setGrossWeight(calculationService.scaleWeight(request.getGrossWeight()));
        bill.setPattiWeight(calculationService.scaleWeight(defaultIfNull(request.getPattiWeight(), BigDecimal.ZERO)));
        bill.setBoxCount(request.getBoxCount() != null ? request.getBoxCount() : 0);
        bill.setNetWeight(calculationService.scaleWeight(calc.baseNetWeight()));
        bill.setDandaWeight(calculationService.scaleWeight(calc.dandaWeight()));
        bill.setTutWastage(calculationService.scaleWeight(defaultIfNull(request.getTutWastage(), BigDecimal.ZERO)));
        bill.setFinalNetWeight(calculationService.scaleWeight(calc.chargeableWeight()));
        bill.setRatePerKg(calculationService.scaleWeight(request.getRatePerKg()));
        bill.setTotalAmount(calculationService.scaleMoney(calc.totalAmount()));
        bill.setMajuri(calculationService.scaleMoney(defaultIfNull(request.getMajuri(), BigDecimal.ZERO)));
        bill.setNetAmount(calculationService.scaleMoney(calc.netAmount()));
        bill.setFarmer(farmer);
        bill.setUpdatedAt(LocalDateTime.now());
        bill.setUpdatedBy(currentUser.getId());
    }
}
