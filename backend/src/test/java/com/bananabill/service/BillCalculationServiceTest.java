package com.bananabill.service;

import com.bananabill.config.BillingConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.math.RoundingMode;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for BillCalculationService
 * 
 * Tests the core billing formulas:
 * 1. Base Net Weight = Gross - Patti - (BoxCount × BoxWeight)
 * 2. Danda = Net × 7%
 * 3. Chargeable Weight = Net + Danda + Tut
 * 4. Total Amount = Chargeable × Rate
 * 5. Net Amount = Total - Majuri
 */
class BillCalculationServiceTest {

    private BillCalculationService calculationService;
    private BillingConfig billingConfig;

    @BeforeEach
    void setUp() {
        billingConfig = new BillingConfig();
        // Set default values (same as application.properties)
        billingConfig.setDandaPercentage(new BigDecimal("0.07"));
        billingConfig.setBoxWeightKg(new BigDecimal("1.0"));
        billingConfig.setWeightScale(2);
        billingConfig.setMoneyScale(2);
        billingConfig.setRoundingMode(RoundingMode.HALF_UP);

        calculationService = new BillCalculationService(billingConfig);
    }

    @Nested
    @DisplayName("Base Net Weight Calculation")
    class BaseNetWeightTests {

        @Test
        @DisplayName("Should calculate base net weight correctly")
        void testCalculateBaseNetWeight() {
            // Given
            BigDecimal grossWeight = new BigDecimal("100.00");
            BigDecimal pattiWeight = new BigDecimal("5.00");
            int boxCount = 10; // 10 boxes × 1kg = 10kg

            // When
            BigDecimal result = calculationService.calculateBaseNetWeight(
                    grossWeight, pattiWeight, boxCount);

            // Then: 100 - 5 - 10 = 85
            assertEquals(new BigDecimal("85.00"), result);
        }

        @Test
        @DisplayName("Should return zero if result would be negative")
        void testNegativeResultReturnsZero() {
            // Given: Gross weight less than deductions
            BigDecimal grossWeight = new BigDecimal("10.00");
            BigDecimal pattiWeight = new BigDecimal("5.00");
            int boxCount = 10; // 10kg deduction

            // When
            BigDecimal result = calculationService.calculateBaseNetWeight(
                    grossWeight, pattiWeight, boxCount);

            // Then: Should be zero, not negative
            assertEquals(BigDecimal.ZERO, result);
        }

        @Test
        @DisplayName("Should handle zero patti and zero boxes")
        void testZeroDeductions() {
            BigDecimal grossWeight = new BigDecimal("100.00");
            BigDecimal pattiWeight = BigDecimal.ZERO;
            int boxCount = 0;

            BigDecimal result = calculationService.calculateBaseNetWeight(
                    grossWeight, pattiWeight, boxCount);

            assertEquals(new BigDecimal("100.00"), result);
        }
    }

    @Nested
    @DisplayName("Danda Weight Calculation")
    class DandaWeightTests {

        @Test
        @DisplayName("Should calculate 7% danda correctly")
        void testCalculateDandaWeight() {
            BigDecimal baseNetWeight = new BigDecimal("100.00");

            BigDecimal result = calculationService.calculateDandaWeight(baseNetWeight);

            // 7% of 100 = 7
            assertEquals(new BigDecimal("7.00"), result);
        }

        @Test
        @DisplayName("Should handle fractional danda with rounding")
        void testDandaWithRounding() {
            BigDecimal baseNetWeight = new BigDecimal("85.00");

            BigDecimal result = calculationService.calculateDandaWeight(baseNetWeight);

            // 7% of 85 = 5.95
            assertEquals(new BigDecimal("5.95"), result);
        }

        @Test
        @DisplayName("Should return zero danda for zero weight")
        void testZeroDanda() {
            BigDecimal result = calculationService.calculateDandaWeight(BigDecimal.ZERO);
            assertEquals(new BigDecimal("0.00"), result);
        }
    }

    @Nested
    @DisplayName("Chargeable Weight Calculation")
    class ChargeableWeightTests {

        @Test
        @DisplayName("Should ADD danda and tut to base weight (not subtract)")
        void testChargeableWeightAddsNotSubtracts() {
            BigDecimal baseNetWeight = new BigDecimal("85.00");
            BigDecimal dandaWeight = new BigDecimal("5.95");
            BigDecimal tutWastage = new BigDecimal("2.00");

            BigDecimal result = calculationService.calculateChargeableWeight(
                    baseNetWeight, dandaWeight, tutWastage);

            // 85 + 5.95 + 2 = 92.95
            assertEquals(new BigDecimal("92.95"), result);
        }

        @Test
        @DisplayName("Should handle zero tut wastage")
        void testZeroTut() {
            BigDecimal baseNetWeight = new BigDecimal("100.00");
            BigDecimal dandaWeight = new BigDecimal("7.00");
            BigDecimal tutWastage = BigDecimal.ZERO;

            BigDecimal result = calculationService.calculateChargeableWeight(
                    baseNetWeight, dandaWeight, tutWastage);

            assertEquals(new BigDecimal("107.00"), result);
        }
    }

    @Nested
    @DisplayName("Total Amount Calculation")
    class TotalAmountTests {

        @Test
        @DisplayName("Should calculate total amount correctly")
        void testCalculateTotalAmount() {
            BigDecimal chargeableWeight = new BigDecimal("92.95");
            BigDecimal ratePerKg = new BigDecimal("50.00");

            BigDecimal result = calculationService.calculateTotalAmount(
                    chargeableWeight, ratePerKg);

            // 92.95 × 50 = 4647.50
            assertEquals(new BigDecimal("4647.50"), result);
        }

        @Test
        @DisplayName("Should handle decimal rates")
        void testDecimalRate() {
            BigDecimal chargeableWeight = new BigDecimal("100.00");
            BigDecimal ratePerKg = new BigDecimal("45.50");

            BigDecimal result = calculationService.calculateTotalAmount(
                    chargeableWeight, ratePerKg);

            // 100 × 45.50 = 4550
            assertEquals(new BigDecimal("4550.00"), result);
        }
    }

    @Nested
    @DisplayName("Net Amount Calculation")
    class NetAmountTests {

        @Test
        @DisplayName("Should subtract majuri from total")
        void testCalculateNetAmount() {
            BigDecimal totalAmount = new BigDecimal("4647.50");
            BigDecimal majuri = new BigDecimal("500.00");

            BigDecimal result = calculationService.calculateNetAmount(totalAmount, majuri);

            // 4647.50 - 500 = 4147.50
            assertEquals(new BigDecimal("4147.50"), result);
        }

        @Test
        @DisplayName("Should return zero if majuri exceeds total")
        void testMajuriExceedsTotal() {
            BigDecimal totalAmount = new BigDecimal("500.00");
            BigDecimal majuri = new BigDecimal("1000.00");

            BigDecimal result = calculationService.calculateNetAmount(totalAmount, majuri);

            // Should be zero, not negative
            assertEquals(BigDecimal.ZERO, result);
        }

        @Test
        @DisplayName("Should handle zero majuri")
        void testZeroMajuri() {
            BigDecimal totalAmount = new BigDecimal("5000.00");

            BigDecimal result = calculationService.calculateNetAmount(
                    totalAmount, BigDecimal.ZERO);

            assertEquals(new BigDecimal("5000.00"), result);
        }
    }

    @Nested
    @DisplayName("Complete Bill Calculation")
    class CompleteBillCalculationTests {

        @Test
        @DisplayName("Should calculate complete bill correctly")
        void testCalculateBill() {
            // Given: Real-world scenario
            BigDecimal grossWeight = new BigDecimal("100.00");
            BigDecimal pattiWeight = new BigDecimal("5.00");
            int boxCount = 10;
            BigDecimal tutWastage = new BigDecimal("2.00");
            BigDecimal ratePerKg = new BigDecimal("50.00");
            BigDecimal majuri = new BigDecimal("500.00");

            // When
            BillCalculationService.BillCalculationResult result = calculationService.calculateBill(
                    grossWeight, pattiWeight, boxCount,
                    tutWastage, ratePerKg, majuri);

            // Then
            // Base Net: 100 - 5 - 10 = 85
            assertEquals(new BigDecimal("85.00"), result.baseNetWeight());

            // Danda: 85 × 0.07 = 5.95
            assertEquals(new BigDecimal("5.95"), result.dandaWeight());

            // Chargeable: 85 + 5.95 + 2 = 92.95
            assertEquals(new BigDecimal("92.95"), result.chargeableWeight());

            // Total: 92.95 × 50 = 4647.50
            assertEquals(new BigDecimal("4647.50"), result.totalAmount());

            // Net: 4647.50 - 500 = 4147.50
            assertEquals(new BigDecimal("4147.50"), result.netAmount());
        }

        @Test
        @DisplayName("Should handle edge case with all zero inputs")
        void testAllZeroInputs() {
            BillCalculationService.BillCalculationResult result = calculationService.calculateBill(
                    BigDecimal.ZERO, BigDecimal.ZERO, 0,
                    BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO);

            // Use compareTo for BigDecimal comparison (scale-independent)
            assertEquals(0, result.baseNetWeight().compareTo(BigDecimal.ZERO));
            assertEquals(0, result.dandaWeight().compareTo(BigDecimal.ZERO));
            assertEquals(0, result.chargeableWeight().compareTo(BigDecimal.ZERO));
            assertEquals(0, result.totalAmount().compareTo(BigDecimal.ZERO));
            assertEquals(0, result.netAmount().compareTo(BigDecimal.ZERO));
        }

        @Test
        @DisplayName("Should handle large numbers correctly")
        void testLargeNumbers() {
            BigDecimal grossWeight = new BigDecimal("10000.00");
            BigDecimal pattiWeight = new BigDecimal("500.00");
            int boxCount = 100;
            BigDecimal tutWastage = new BigDecimal("50.00");
            BigDecimal ratePerKg = new BigDecimal("100.00");
            BigDecimal majuri = new BigDecimal("5000.00");

            BillCalculationService.BillCalculationResult result = calculationService.calculateBill(
                    grossWeight, pattiWeight, boxCount,
                    tutWastage, ratePerKg, majuri);

            // Base Net: 10000 - 500 - 100 = 9400
            assertEquals(new BigDecimal("9400.00"), result.baseNetWeight());

            // Total should be positive and make sense
            assertTrue(result.totalAmount().compareTo(BigDecimal.ZERO) > 0);
            assertTrue(result.netAmount().compareTo(BigDecimal.ZERO) > 0);
        }
    }
}
