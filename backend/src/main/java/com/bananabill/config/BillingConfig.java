package com.bananabill.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Billing Configuration for Banana Bill System
 * 
 * DOMAIN RULES (Agricultural Commodity Billing):
 * - Gross Weight = Total measured weight on scale
 * - Base Net Weight = Gross - Patti - (BoxCount × BoxWeight)
 * - Chargeable Weight = Base Net Weight + Danda + Tut
 * 
 * NOTE: Danda and Tut are ADDED (not subtracted) to calculate
 * chargeable weight. This is the correct domain formula.
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "billing")
public class BillingConfig {

    /**
     * Weight per box in KG (default: 1.0)
     * Used to calculate: Net = Gross - Patti - (BoxCount × BoxWeight)
     */
    private BigDecimal boxWeightKg = new BigDecimal("1.0");

    /**
     * Danda percentage deduction (default: 7%)
     * Danda Weight = Base Net Weight × dandaPercentage
     * NOTE: Danda is ADDED to Net for chargeable weight
     */
    private BigDecimal dandaPercentage = new BigDecimal("0.07");

    /**
     * Decimal scale for weight calculations (default: 2)
     */
    private int weightScale = 2;

    /**
     * Decimal scale for money calculations (default: 2)
     */
    private int moneyScale = 2;

    /**
     * Rounding mode for all calculations (default: HALF_UP)
     */
    private RoundingMode roundingMode = RoundingMode.HALF_UP;

    /**
     * Maximum bills per month per sequence (default: 99999)
     */
    private int maxBillsPerMonth = 99999;

    /**
     * Allow overpayment tracking (default: true)
     */
    private boolean trackOverpayment = true;

    /**
     * Default page size for queries (default: 100)
     */
    private int defaultPageSize = 100;

    /**
     * Email domain for auto-generated emails (default: bananabill.app)
     */
    private String emailDomain = "bananabill.app";
}
