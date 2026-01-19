package com.bananabill.exception;

/**
 * Exception for payment-related errors
 * Thrown when payment validation fails or payment cannot be processed
 */
public class PaymentException extends RuntimeException {

    private final String errorCode;
    private final String billId;

    public PaymentException(String message) {
        super(message);
        this.errorCode = "PAYMENT_ERROR";
        this.billId = null;
    }

    public PaymentException(String message, String errorCode, String billId) {
        super(message);
        this.errorCode = errorCode;
        this.billId = billId;
    }

    public String getErrorCode() {
        return errorCode;
    }

    public String getBillId() {
        return billId;
    }

    // Common factory methods
    public static PaymentException invalidAmount() {
        return new PaymentException("Payment amount must be positive and non-null", "INVALID_AMOUNT", null);
    }

    public static PaymentException billNotFound(String billId) {
        return new PaymentException("Bill not found: " + billId, "BILL_NOT_FOUND", billId);
    }

    public static PaymentException alreadyPaid(String billId) {
        return new PaymentException("Bill is already fully paid", "ALREADY_PAID", billId);
    }

    public static PaymentException authenticationRequired() {
        return new PaymentException("User authentication required", "AUTH_REQUIRED", null);
    }
}
