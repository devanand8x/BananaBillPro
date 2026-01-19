package com.bananabill.service;

import com.bananabill.config.BillingConfig;
import com.bananabill.exception.PaymentException;
import com.bananabill.model.Bill;
import com.bananabill.model.PaymentHistory;
import com.bananabill.model.PaymentStatus;
import com.bananabill.model.User;
import com.bananabill.repository.BillRepository;
import com.bananabill.repository.PaymentHistoryRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.bananabill.util.SecurityUtils;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Payment Service - Handles all payment operations
 * 
 * Responsibilities:
 * - Payment recording with validation
 * - Overpayment tracking
 * - Payment history audit trail
 * - Payment status management
 */
@Service
public class PaymentService {

    private static final Logger logger = LoggerFactory.getLogger(PaymentService.class);

    private final BillRepository billRepository;
    private final PaymentHistoryRepository paymentHistoryRepository;
    private final BillingConfig billingConfig;

    public PaymentService(BillRepository billRepository,
            PaymentHistoryRepository paymentHistoryRepository,
            BillingConfig billingConfig) {
        this.billRepository = billRepository;
        this.paymentHistoryRepository = paymentHistoryRepository;
        this.billingConfig = billingConfig;
    }

    /**
     * Record a payment with full validation and overpayment tracking
     */
    public Bill recordPayment(String billId, BigDecimal amount) {
        validatePaymentAmount(amount);

        Bill bill = billRepository.findById(billId)
                .orElseThrow(() -> PaymentException.billNotFound(billId));

        BigDecimal currentPaid = getCurrentPaidAmount(bill);
        BigDecimal newPaidAmount = currentPaid.add(amount);
        BigDecimal netAmount = bill.getNetAmount() != null ? bill.getNetAmount() : BigDecimal.ZERO;

        updateBillPayment(bill, newPaidAmount);
        determinePaymentStatus(bill, newPaidAmount, netAmount);

        logger.info("Payment of {} recorded for bill {}. Status: {}",
                amount, billId, bill.getPaymentStatus());

        Bill savedBill = billRepository.save(bill);
        recordPaymentHistory(savedBill, amount, currentPaid, newPaidAmount,
                PaymentHistory.PaymentType.PAYMENT, null, null);

        return savedBill;
    }

    /**
     * Mark bill as fully paid (quick operation)
     */
    public Bill markAsPaid(String billId) {
        Bill bill = billRepository.findById(billId)
                .orElseThrow(() -> PaymentException.billNotFound(billId));

        bill.setPaymentStatus(PaymentStatus.PAID);
        bill.setPaidAmount(bill.getNetAmount());
        bill.setPaymentDate(LocalDateTime.now());
        bill.setUpdatedAt(LocalDateTime.now());
        bill.setUpdatedBy(getCurrentUser().getId());

        return billRepository.save(bill);
    }

    /**
     * Update payment status with validation
     */
    public Bill updatePaymentStatus(String billId, PaymentStatus status, BigDecimal paidAmount) {
        if (paidAmount != null && paidAmount.compareTo(BigDecimal.ZERO) < 0) {
            throw PaymentException.invalidAmount();
        }

        Bill bill = billRepository.findById(billId)
                .orElseThrow(() -> PaymentException.billNotFound(billId));

        bill.setPaymentStatus(status);
        if (paidAmount != null) {
            bill.setPaidAmount(scaleMoney(paidAmount));
        }
        if (status == PaymentStatus.PAID) {
            bill.setPaymentDate(LocalDateTime.now());
        }
        bill.setUpdatedAt(LocalDateTime.now());
        bill.setUpdatedBy(getCurrentUser().getId());

        return billRepository.save(bill);
    }

    /**
     * Get payment history for a bill
     */
    public List<PaymentHistory> getPaymentHistory(String billId) {
        return paymentHistoryRepository.findByBillIdOrderByCreatedAtDesc(billId);
    }

    // ==================== PRIVATE HELPERS ====================

    private void validatePaymentAmount(BigDecimal amount) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw PaymentException.invalidAmount();
        }
    }

    private BigDecimal getCurrentPaidAmount(Bill bill) {
        return bill.getPaidAmount() != null ? bill.getPaidAmount() : BigDecimal.ZERO;
    }

    private void updateBillPayment(Bill bill, BigDecimal newPaidAmount) {
        bill.setPaidAmount(scaleMoney(newPaidAmount));
        bill.setPaymentDate(LocalDateTime.now());
        bill.setUpdatedAt(LocalDateTime.now());
        bill.setUpdatedBy(getCurrentUser().getId());
    }

    private void determinePaymentStatus(Bill bill, BigDecimal newPaidAmount, BigDecimal netAmount) {
        int comparison = newPaidAmount.compareTo(netAmount);

        if (comparison >= 0) {
            bill.setPaymentStatus(PaymentStatus.PAID);
            handleOverpayment(bill, newPaidAmount, netAmount, comparison);
        } else if (newPaidAmount.compareTo(BigDecimal.ZERO) > 0) {
            bill.setPaymentStatus(PaymentStatus.PARTIAL);
        }
    }

    private void handleOverpayment(Bill bill, BigDecimal newPaidAmount,
            BigDecimal netAmount, int comparison) {
        if (comparison > 0 && billingConfig.isTrackOverpayment()) {
            BigDecimal excess = newPaidAmount.subtract(netAmount);
            bill.setAdvanceAmount(scaleMoney(excess));
            logger.info("Bill {} overpaid by {}. Tracked as advance.", bill.getId(), excess);
        }
    }

    private void recordPaymentHistory(Bill bill, BigDecimal amount,
            BigDecimal previousPaid, BigDecimal newPaid,
            PaymentHistory.PaymentType type, String method, String notes) {
        User currentUser = getCurrentUser();

        PaymentHistory history = new PaymentHistory();
        history.setBillId(bill.getId());
        history.setBillNumber(bill.getBillNumber());
        history.setFarmerId(bill.getFarmerId());

        if (bill.getFarmer() != null) {
            history.setFarmerName(bill.getFarmer().getName());
            history.setFarmerMobile(bill.getFarmer().getMobileNumber());
        }

        history.setAmount(amount);
        history.setPreviousPaidAmount(previousPaid);
        history.setNewPaidAmount(newPaid);
        history.setBillNetAmount(bill.getNetAmount());
        history.setPaymentType(type);
        history.setPaymentMethod(method);
        history.setNotes(notes);
        history.setCreatedBy(currentUser.getId());
        history.setCreatedByName(currentUser.getName());

        try {
            paymentHistoryRepository.save(history);
            logger.debug("Payment history recorded for bill {}", bill.getBillNumber());
        } catch (Exception e) {
            // Log but don't fail - payment already succeeded
            logger.error("Failed to record payment history for bill {}. Payment was recorded successfully.",
                    bill.getBillNumber(), e);
        }
    }

    private User getCurrentUser() {
        return SecurityUtils.getCurrentUser();
    }

    private BigDecimal scaleMoney(BigDecimal value) {
        return value.setScale(billingConfig.getMoneyScale(), billingConfig.getRoundingMode());
    }
}
