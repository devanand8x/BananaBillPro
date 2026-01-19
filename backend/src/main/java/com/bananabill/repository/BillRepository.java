package com.bananabill.repository;

import com.bananabill.model.Bill;
import com.bananabill.model.PaymentStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.repository.Aggregation;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Bill Repository - Database access layer
 * 
 * PERFORMANCE OPTIMIZED:
 * - All filters pushed to database queries
 * - Aggregation pipelines for statistics
 * - Pagination on all list queries
 * 
 * REQUIRED INDEXES (run in MongoDB shell):
 * db.bills.createIndex({ "createdAt": -1 })
 * db.bills.createIndex({ "farmerId": 1, "createdAt": -1 })
 * db.bills.createIndex({ "paymentStatus": 1 })
 * db.bills.createIndex({ "farmerId": 1, "paymentStatus": 1 })
 * db.bills.createIndex({ "billNumber": 1 }, { unique: true })
 */
@Repository
public interface BillRepository extends MongoRepository<Bill, String> {

        // ==================== BASIC QUERIES ====================

        Optional<Bill> findByBillNumber(String billNumber);

        List<Bill> findByFarmerId(String farmerId);

        List<Bill> findByCreatedAtBetween(LocalDateTime start, LocalDateTime end);

        Boolean existsByBillNumber(String billNumber);

        // ==================== PAGINATED QUERIES ====================

        Page<Bill> findByFarmerId(String farmerId, Pageable pageable);

        @Query(value = "{}", sort = "{ 'createdAt': -1 }")
        List<Bill> findRecentBills(Pageable pageable);

        Page<Bill> findByPaymentStatus(PaymentStatus status, Pageable pageable);

        // ==================== FILTERED QUERIES (PUSHED TO DB) ====================

        /**
         * Find bills by farmer with all filters applied at database level
         * Replaces in-memory filtering
         */
        @Query("{ 'farmerId': ?0, " +
                        "$and: [ " +
                        "  { $or: [ { $expr: { $eq: [?1, null] } }, { 'createdAt': { $gte: ?1 } } ] }, " +
                        "  { $or: [ { $expr: { $eq: [?2, null] } }, { 'createdAt': { $lte: ?2 } } ] }, " +
                        "  { $or: [ { $expr: { $eq: [?3, null] } }, { 'paymentStatus': ?3 } ] } " +
                        "] }")
        List<Bill> findByFarmerFiltered(String farmerId, LocalDateTime startDate,
                        LocalDateTime endDate, PaymentStatus status,
                        Sort sort);

        /**
         * Paginated version for large datasets
         */
        @Query("{ 'farmerId': ?0, " +
                        "$and: [ " +
                        "  { $or: [ { $expr: { $eq: [?1, null] } }, { 'createdAt': { $gte: ?1 } } ] }, " +
                        "  { $or: [ { $expr: { $eq: [?2, null] } }, { 'createdAt': { $lte: ?2 } } ] }, " +
                        "  { $or: [ { $expr: { $eq: [?3, null] } }, { 'paymentStatus': ?3 } ] } " +
                        "] }")
        Page<Bill> findByFarmerFilteredPaged(String farmerId, LocalDateTime startDate,
                        LocalDateTime endDate, PaymentStatus status,
                        Pageable pageable);

        /**
         * Find bills by date range with payment status filter
         */
        List<Bill> findByCreatedAtBetweenAndPaymentStatus(
                        LocalDateTime startDate, LocalDateTime endDate,
                        PaymentStatus status, Sort sort);

        List<Bill> findByCreatedAtBetweenOrderByCreatedAtDesc(
                        LocalDateTime startDate, LocalDateTime endDate);

        // ==================== UNPAID BILLS (OPTIMIZED) ====================

        @Query("{ '$or': [ { 'paymentStatus': null }, { 'paymentStatus': 'UNPAID' } ] }")
        List<Bill> findUnpaidBills(Sort sort);

        @Query("{ '$or': [ { 'paymentStatus': null }, { 'paymentStatus': 'UNPAID' } ] }")
        Page<Bill> findUnpaidBillsPaged(Pageable pageable);

        // ==================== OVERDUE BILLS ====================

        List<Bill> findByDueDateBeforeAndPaymentStatusNot(
                        LocalDateTime dueDate, PaymentStatus status);

        Page<Bill> findByDueDateBeforeAndPaymentStatusNot(
                        LocalDateTime dueDate, PaymentStatus status, Pageable pageable);

        // ==================== COUNT QUERIES (O(1) with index) ====================

        Long countByCreatedAtAfter(LocalDateTime dateTime);

        Long countByPaymentStatus(PaymentStatus status);

        @Query(value = "{ '$or': [ { 'paymentStatus': null }, { 'paymentStatus': 'UNPAID' } ] }", count = true)
        Long countUnpaidBills();

        Long countByFarmerId(String farmerId);

        Long countByFarmerIdAndPaymentStatus(String farmerId, PaymentStatus status);

        Long countByCreatedAtBetween(LocalDateTime startDate, LocalDateTime endDate);

        // ==================== SIMPLE COUNT FOR STATS ====================
        // Note: Complex aggregations removed - use MongoTemplate for aggregations

}
