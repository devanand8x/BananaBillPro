package com.bananabill.dto;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Request DTO for creating/updating bills
 * Uses Bean Validation (JSR-380) for field validation
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateBillRequest {

    @NotBlank(message = "Farmer ID is required")
    private String farmerId;

    @Size(max = 20, message = "Vehicle number must be at most 20 characters")
    private String vehicleNumber;

    // Input values - backend calculates the rest
    @NotNull(message = "Gross weight is required")
    @DecimalMin(value = "0.01", message = "Gross weight must be greater than 0")
    @DecimalMax(value = "99999.99", message = "Gross weight is too large")
    private BigDecimal grossWeight;

    @DecimalMin(value = "0", message = "Patti weight cannot be negative")
    private BigDecimal pattiWeight = BigDecimal.ZERO;

    @Min(value = 0, message = "Box count cannot be negative")
    @Max(value = 9999, message = "Box count is too large")
    private Integer boxCount = 0;

    @DecimalMin(value = "0", message = "Tut wastage cannot be negative")
    private BigDecimal tutWastage = BigDecimal.ZERO;

    @NotNull(message = "Rate per kg is required")
    @DecimalMin(value = "0.01", message = "Rate must be greater than 0")
    @DecimalMax(value = "9999.99", message = "Rate is too large")
    private BigDecimal ratePerKg;

    @DecimalMin(value = "0", message = "Majuri cannot be negative")
    private BigDecimal majuri = BigDecimal.ZERO;

    // Backend will calculate:
    // - netWeight = grossWeight - pattiWeight - boxCount
    // - dandaWeight = netWeight * 7%
    // - finalNetWeight = netWeight + dandaWeight + tutWastage
    // - totalAmount = finalNetWeight * ratePerKg
    // - netAmount = totalAmount - majuri
}
