package com.bananabill.service;

import com.bananabill.model.Counter;
import org.springframework.data.mongodb.core.FindAndModifyOptions;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;

/**
 * Counter Service for atomic sequence generation
 * Uses MongoDB's findAndModify for thread-safe incrementing
 * 
 * This prevents race conditions when generating bill numbers
 * under high concurrent load (even 1 lakh simultaneous requests)
 */
@Service
public class CounterService {

    private static final Logger logger = LoggerFactory.getLogger(CounterService.class);

    private final MongoTemplate mongoTemplate;

    public CounterService(MongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
    }

    /**
     * Get next sequence number atomically
     * Thread-safe - MongoDB guarantees atomic increment
     * 
     * @param sequenceName The name of the sequence (e.g., "bill_2601" for Jan 2026)
     * @return Next sequence number
     */
    public long getNextSequence(String sequenceName) {
        Query query = new Query(Criteria.where("_id").is(sequenceName));
        Update update = new Update().inc("sequence", 1);
        FindAndModifyOptions options = new FindAndModifyOptions()
                .returnNew(true) // Return updated document
                .upsert(true); // Create if not exists

        Counter counter = mongoTemplate.findAndModify(query, update, options, Counter.class);

        return counter != null ? counter.getSequence() : 1;
    }

    /**
     * Generate unique bill number atomically
     * Format: BB{YYMM}{0001-9999}
     * Example: BB2601001 (January 2026, bill #1)
     * 
     * Thread-safe for concurrent requests
     */
    public String generateBillNumber() {
        try {
            LocalDate now = LocalDate.now();
            String yearMonth = now.format(DateTimeFormatter.ofPattern("yyMM"));
            String sequenceName = "bill_" + yearMonth;

            long sequence = getNextSequence(sequenceName);

            // Format: BB + YYMM + 00001 (5-digit for up to 99,999 bills/month)
            return "BB" + yearMonth + String.format("%05d", sequence);
        } catch (DataAccessException e) {
            logger.error("MongoDB unavailable for sequence generation", e);
            throw new RuntimeException("Unable to generate bill number. Database unavailable.", e);
        }
    }

    /**
     * Reset sequence for a specific month (admin use only)
     */
    public void resetSequence(String sequenceName, long startValue) {
        Query query = new Query(Criteria.where("_id").is(sequenceName));
        Update update = new Update().set("sequence", startValue);
        mongoTemplate.upsert(query, update, Counter.class);
    }
}
