package com.bananabill.dto.response;

import com.bananabill.model.Bill;
import com.bananabill.model.PaymentStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Bill Response DTO - Decoupled from entity
 * Only exposes what API consumers need
 */
public record BillResponse(
                String id,
                String billNumber,
                FarmerSummary farmer,
                String vehicleNumber,
                WeightDetails weight,
                PaymentDetails payment,
                PaymentStatus paymentStatus, // Added at root level for frontend compatibility
                LocalDateTime createdAt) {
        /**
         * Nested record for farmer summary
         */
        public record FarmerSummary(
                        String id,
                        String name,
                        String mobile,
                        String village) {
        }

        /**
         * Nested record for weight details
         */
        public record WeightDetails(
                        BigDecimal grossWeight,
                        BigDecimal pattiWeight,
                        Integer boxCount,
                        BigDecimal netWeight,
                        BigDecimal dandaWeight,
                        BigDecimal tutWastage,
                        BigDecimal finalNetWeight) {
        }

        /**
         * Nested record for payment details
         */
        public record PaymentDetails(
                        BigDecimal ratePerKg,
                        BigDecimal totalAmount,
                        BigDecimal majuri,
                        BigDecimal netAmount,
                        PaymentStatus status,
                        BigDecimal paidAmount,
                        LocalDateTime paymentDate) {
        }

        /**
         * Factory method to convert entity to DTO
         */
        public static BillResponse from(Bill bill) {
                FarmerSummary farmer = null;
                if (bill.getFarmer() != null) {
                        farmer = new FarmerSummary(
                                        bill.getFarmer().getId(),
                                        bill.getFarmer().getName(),
                                        bill.getFarmer().getMobileNumber(),
                                        bill.getFarmer().getAddress());
                }

                WeightDetails weight = new WeightDetails(
                                bill.getGrossWeight(),
                                bill.getPattiWeight(),
                                bill.getBoxCount(),
                                bill.getNetWeight(),
                                bill.getDandaWeight(),
                                bill.getTutWastage(),
                                bill.getFinalNetWeight());

                // Default to UNPAID if null (for legacy database records)
                PaymentStatus status = bill.getPaymentStatus() != null
                                ? bill.getPaymentStatus()
                                : PaymentStatus.UNPAID;

                PaymentDetails payment = new PaymentDetails(
                                bill.getRatePerKg(),
                                bill.getTotalAmount(),
                                bill.getMajuri(),
                                bill.getNetAmount(),
                                status,
                                bill.getPaidAmount(),
                                bill.getPaymentDate());

                return new BillResponse(
                                bill.getId(),
                                bill.getBillNumber(),
                                farmer,
                                bill.getVehicleNumber(),
                                weight,
                                payment,
                                status, // paymentStatus at root level
                                bill.getCreatedAt());
        }
}
