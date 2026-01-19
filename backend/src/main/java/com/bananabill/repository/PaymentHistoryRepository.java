package com.bananabill.repository;

import com.bananabill.model.PaymentHistory;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Repository for Payment History
 * Provides audit trail for all payments
 */
@Repository
public interface PaymentHistoryRepository extends MongoRepository<PaymentHistory, String> {

    List<PaymentHistory> findByBillIdOrderByCreatedAtDesc(String billId);

    List<PaymentHistory> findByBillNumberOrderByCreatedAtDesc(String billNumber);

    List<PaymentHistory> findByFarmerIdOrderByCreatedAtDesc(String farmerId);

    List<PaymentHistory> findByCreatedByOrderByCreatedAtDesc(String userId);

    List<PaymentHistory> findByCreatedAtBetweenOrderByCreatedAtDesc(LocalDateTime start, LocalDateTime end);

    Long countByBillId(String billId);
}
