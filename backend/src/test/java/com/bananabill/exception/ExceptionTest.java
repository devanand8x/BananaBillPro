package com.bananabill.exception;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ExceptionTest {

    @Test
    void resourceNotFoundException_ShouldContainMessage() {
        ResourceNotFoundException ex = new ResourceNotFoundException("User not found");
        assertEquals("User not found", ex.getMessage());
    }

    @Test
    void resourceNotFoundException_WithResourceDetails_ShouldContainMessage() {
        ResourceNotFoundException ex = new ResourceNotFoundException("User", "id", "123");
        assertTrue(ex.getMessage().contains("User"));
    }

    @Test
    void validationException_ShouldContainMessage() {
        ValidationException ex = new ValidationException("Invalid input");
        assertEquals("Invalid input", ex.getMessage());
    }

    @Test
    void validationException_WithField_ShouldContainFieldName() {
        ValidationException ex = new ValidationException("email", "Email is required");
        assertTrue(ex.getMessage().contains("Email is required"));
    }

    @Test
    void billException_NotFound_ShouldHaveCorrectMessage() {
        BillException ex = BillException.notFound("B001");
        assertTrue(ex.getMessage().contains("B001"));
    }

    @Test
    void paymentException_InvalidAmount_ShouldHaveCorrectMessage() {
        PaymentException ex = PaymentException.invalidAmount();
        assertNotNull(ex.getMessage());
    }

    @Test
    void paymentException_BillNotFound_ShouldHaveCorrectMessage() {
        PaymentException ex = PaymentException.billNotFound("bill-123");
        assertTrue(ex.getMessage().contains("bill-123"));
    }

    @Test
    void businessException_ShouldContainMessage() {
        BusinessException ex = new BusinessException("Business rule violated");
        assertEquals("Business rule violated", ex.getMessage());
    }
}
