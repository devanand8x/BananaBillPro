package com.bananabill.model;

/**
 * Payment status for bills
 * Tracks whether trader has paid the farmer
 */
public enum PaymentStatus {
    PAID, // Full payment made to farmer
    UNPAID, // No payment made yet
    PARTIAL // Partial payment made
}
