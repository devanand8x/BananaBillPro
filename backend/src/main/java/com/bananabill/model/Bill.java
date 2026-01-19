package com.bananabill.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Version;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "bills")
public class Bill {

    @Id
    private String id;

    @Version
    private Long version; // Optimistic locking - prevents concurrent update conflicts

    @Indexed(unique = true)
    private String billNumber;

    private String farmerId;

    private String vehicleNumber;

    // Weight calculations (all in KG)
    private BigDecimal grossWeight;
    private BigDecimal pattiWeight;
    private Integer boxCount;
    private BigDecimal netWeight;
    private BigDecimal dandaWeight;
    private BigDecimal tutWastage;
    private BigDecimal finalNetWeight;

    // Payment calculations
    private BigDecimal ratePerKg;
    private BigDecimal totalAmount;
    private BigDecimal majuri;
    private BigDecimal netAmount;

    private String createdBy;

    @CreatedDate
    private LocalDateTime createdAt;

    // Embedded farmer details for join queries (denormalized for performance)
    private Farmer farmer;

    // Payment tracking fields (Trader pays Farmer)
    private PaymentStatus paymentStatus = PaymentStatus.UNPAID;
    private BigDecimal paidAmount = BigDecimal.ZERO;
    private LocalDateTime paymentDate;
    private LocalDateTime dueDate;
    private LocalDateTime lastReminderSent;

    // Overpayment tracking (when paid > netAmount)
    private BigDecimal advanceAmount = BigDecimal.ZERO;

    // Audit fields
    private LocalDateTime updatedAt;
    private String updatedBy;
}
