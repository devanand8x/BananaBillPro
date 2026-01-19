package com.bananabill.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Payment History - Tracks individual payments against a bill
 * 
 * Enables:
 * - Audit trail of all payments
 * - Multiple partial payments tracking
 * - Payment method recording
 * - Refund tracking
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "payment_history")
public class PaymentHistory {

    @Id
    private String id;

    @Indexed
    private String billId;

    @Indexed
    private String billNumber;

    private String farmerId;
    private String farmerName;
    private String farmerMobile;

    private BigDecimal amount;
    private BigDecimal previousPaidAmount;
    private BigDecimal newPaidAmount;
    private BigDecimal billNetAmount;

    private PaymentType paymentType; // PAYMENT, REFUND, ADJUSTMENT
    private String paymentMethod; // CASH, UPI, BANK_TRANSFER, etc.
    private String transactionRef; // UPI ID, cheque number, etc.
    private String notes;

    private String createdBy;
    private String createdByName;

    @CreatedDate
    private LocalDateTime createdAt;

    public enum PaymentType {
        PAYMENT,
        REFUND,
        ADJUSTMENT,
        ADVANCE_USED
    }
}
