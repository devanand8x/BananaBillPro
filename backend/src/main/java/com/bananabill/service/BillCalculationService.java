package com.bananabill.service;

import com.bananabill.config.BillingConfig;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

/**
 * Bill Calculation Service - Pure calculation logic
 * 
 * DOMAIN FORMULA (Agricultural Commodity Billing):
 * 1. Base Net Weight = Gross Weight - Patti Weight - (BoxCount × BoxWeight)
 * 2. Danda Weight = Base Net Weight × Danda Percentage (default 7%)
 * 3. Chargeable Weight = Base Net Weight + Danda Weight + Tut Wastage
 * ⚠️ NOTE: Danda and Tut are ADDED (not subtracted) - domain rule
 * 4. Total Amount = Chargeable Weight × Rate per Kg
 * 5. Net Amount = Total Amount - Majuri (labor cost)
 * 
 * This service is stateless and performs only calculations.
 * No database access or side effects.
 */
@Service
public class BillCalculationService {

    private final BillingConfig billingConfig;

    public BillCalculationService(BillingConfig billingConfig) {
        this.billingConfig = billingConfig;
    }

    /**
     * Calculate Base Net Weight
     * Formula: Gross - Patti - (BoxCount × BoxWeight)
     * 
     * @param grossWeight Total weight measured
     * @param pattiWeight Patti deduction
     * @param boxCount    Number of boxes
     * @return Base net weight (capped at zero)
     */
    public BigDecimal calculateBaseNetWeight(BigDecimal grossWeight, BigDecimal pattiWeight, int boxCount) {
        BigDecimal boxWeight = billingConfig.getBoxWeightKg()
                .multiply(BigDecimal.valueOf(boxCount));
        BigDecimal result = grossWeight.subtract(pattiWeight).subtract(boxWeight);
        return result.compareTo(BigDecimal.ZERO) < 0 ? BigDecimal.ZERO : result;
    }

    /**
     * Calculate Danda Weight
     * Formula: Base Net Weight × Danda Percentage (configurable, default 7%)
     */
    public BigDecimal calculateDandaWeight(BigDecimal baseNetWeight) {
        return baseNetWeight.multiply(billingConfig.getDandaPercentage())
                .setScale(billingConfig.getWeightScale(), billingConfig.getRoundingMode());
    }

    /**
     * Calculate Chargeable Weight
     * DOMAIN RULE: Chargeable = Base Net + Danda + Tut
     * ⚠️ Danda and Tut are ADDED (not subtracted) - correct for agricultural
     * billing
     */
    public BigDecimal calculateChargeableWeight(BigDecimal baseNetWeight, BigDecimal dandaWeight,
            BigDecimal tutWastage) {
        return baseNetWeight.add(dandaWeight).add(tutWastage);
    }

    /**
     * Calculate Total Amount
     * Formula: Chargeable Weight × Rate per Kg
     */
    public BigDecimal calculateTotalAmount(BigDecimal chargeableWeight, BigDecimal ratePerKg) {
        return chargeableWeight.multiply(ratePerKg)
                .setScale(billingConfig.getMoneyScale(), billingConfig.getRoundingMode());
    }

    /**
     * Calculate Net Amount
     * Formula: Total Amount - Majuri (capped at zero)
     */
    public BigDecimal calculateNetAmount(BigDecimal totalAmount, BigDecimal majuri) {
        BigDecimal result = totalAmount.subtract(majuri);
        return result.compareTo(BigDecimal.ZERO) < 0 ? BigDecimal.ZERO : result;
    }

    /**
     * Complete bill calculation - orchestrates all calculations
     * 
     * @return BillCalculationResult with all calculated values
     */
    public BillCalculationResult calculateBill(BigDecimal grossWeight, BigDecimal pattiWeight,
            int boxCount, BigDecimal tutWastage, BigDecimal ratePerKg, BigDecimal majuri) {

        BigDecimal baseNetWeight = calculateBaseNetWeight(grossWeight, pattiWeight, boxCount);
        BigDecimal dandaWeight = calculateDandaWeight(baseNetWeight);
        BigDecimal chargeableWeight = calculateChargeableWeight(baseNetWeight, dandaWeight, tutWastage);
        BigDecimal totalAmount = calculateTotalAmount(chargeableWeight, ratePerKg);
        BigDecimal netAmount = calculateNetAmount(totalAmount, majuri);

        return new BillCalculationResult(
                baseNetWeight, dandaWeight, chargeableWeight, totalAmount, netAmount);
    }

    /**
     * Scale weight value using configured precision
     */
    public BigDecimal scaleWeight(BigDecimal value) {
        return value.setScale(billingConfig.getWeightScale(), billingConfig.getRoundingMode());
    }

    /**
     * Scale money value using configured precision
     */
    public BigDecimal scaleMoney(BigDecimal value) {
        return value.setScale(billingConfig.getMoneyScale(), billingConfig.getRoundingMode());
    }

    /**
     * Result container for bill calculations
     */
    public record BillCalculationResult(
            BigDecimal baseNetWeight,
            BigDecimal dandaWeight,
            BigDecimal chargeableWeight,
            BigDecimal totalAmount,
            BigDecimal netAmount) {
    }
}
