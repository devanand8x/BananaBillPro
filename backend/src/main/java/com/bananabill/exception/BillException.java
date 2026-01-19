package com.bananabill.exception;

/**
 * Exception for bill-related errors
 * Thrown when bill operations fail
 */
public class BillException extends RuntimeException {

    private final String errorCode;
    private final String billId;

    public BillException(String message) {
        super(message);
        this.errorCode = "BILL_ERROR";
        this.billId = null;
    }

    public BillException(String message, String errorCode, String billId) {
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
    public static BillException notFound(String billId) {
        return new BillException("Bill not found: " + billId, "NOT_FOUND", billId);
    }

    public static BillException farmerNotFound(String farmerId) {
        return new BillException("Farmer not found: " + farmerId, "FARMER_NOT_FOUND", null);
    }

    public static BillException sequenceOverflow(String yearMonth) {
        return new BillException(
                "Maximum bills per month exceeded for " + yearMonth + ". Contact administrator.",
                "SEQUENCE_OVERFLOW", null);
    }

    public static BillException invalidInput(String field, String reason) {
        return new BillException(field + ": " + reason, "INVALID_INPUT", null);
    }

    public static BillException authenticationRequired() {
        return new BillException("User authentication required", "AUTH_REQUIRED", null);
    }

    public static BillException databaseUnavailable() {
        return new BillException("Database temporarily unavailable. Please try again.", "DB_UNAVAILABLE", null);
    }
}
