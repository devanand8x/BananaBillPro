package com.bananabill.config;

import com.bananabill.model.Bill;
import com.bananabill.model.Farmer;
import com.bananabill.model.RefreshToken;
import com.bananabill.model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.index.Index;
import org.springframework.data.mongodb.core.index.IndexDefinition;
import org.springframework.data.mongodb.core.index.IndexOperations;

/**
 * MongoDB Index Configuration for optimal query performance
 * 
 * Index Strategy:
 * 1. Single-field indexes for equality queries
 * 2. Compound indexes for range queries (put equality first, then range)
 * 3. Covered indexes where possible (include projected fields)
 * 4. TTL indexes for automatic expiry
 */
@Configuration
public class MongoIndexConfig {

    private static final Logger logger = LoggerFactory.getLogger(MongoIndexConfig.class);

    @Bean
    public CommandLineRunner createIndexes(MongoTemplate mongoTemplate) {
        return args -> {
            logger.info("Creating MongoDB indexes...");

            createBillIndexes(mongoTemplate);
            createFarmerIndexes(mongoTemplate);
            createUserIndexes(mongoTemplate);
            createRefreshTokenIndexes(mongoTemplate);

            logger.info("MongoDB index creation process completed");
        };
    }

    /**
     * Helper to safely create index without crashing application
     */
    private void ensureIndexSafely(IndexOperations indexOps, IndexDefinition indexDefinition) {
        try {
            indexOps.ensureIndex(indexDefinition);
        } catch (Exception e) {
            String indexName = indexDefinition.getIndexKeys().toString();
            logger.warn(
                    "Failed to create index {}: {}. This is likely due to a conflict with an existing index or data constraint violation.",
                    indexName, e.getMessage());
            // We continue despite error to ensure app starts
        }
    }

    /**
     * Bill Collection Indexes
     * Most queried collection - needs careful optimization
     */
    private void createBillIndexes(MongoTemplate mongoTemplate) {
        IndexOperations billIndexOps = mongoTemplate.indexOps(Bill.class);

        // 1. Unique index on billNumber
        ensureIndexSafely(billIndexOps,
                new Index()
                        .on("billNumber", Sort.Direction.ASC)
                        .unique()
                        .named("idx_bill_number_unique"));

        // 2. Compound index for date range queries with ordering
        ensureIndexSafely(billIndexOps,
                new Index()
                        .on("createdAt", Sort.Direction.DESC)
                        .named("idx_bill_created_desc"));

        // 3. Compound index for user's bills by date
        ensureIndexSafely(billIndexOps,
                new Index()
                        .on("createdBy", Sort.Direction.ASC)
                        .on("createdAt", Sort.Direction.DESC)
                        .named("idx_bill_user_date"));

        // 4. Index for farmer lookup
        ensureIndexSafely(billIndexOps,
                new Index()
                        .on("farmerId", Sort.Direction.ASC)
                        .named("idx_bill_farmer"));

        // 5. Compound index for farmer + payment status
        ensureIndexSafely(billIndexOps,
                new Index()
                        .on("farmerId", Sort.Direction.ASC)
                        .on("paymentStatus", Sort.Direction.ASC)
                        .named("idx_bill_farmer_payment"));

        // 6. Index for payment status queries with ordering
        ensureIndexSafely(billIndexOps,
                new Index()
                        .on("paymentStatus", Sort.Direction.ASC)
                        .on("createdAt", Sort.Direction.DESC)
                        .named("idx_bill_payment_date"));

        // 7. Compound index for overdue bills
        ensureIndexSafely(billIndexOps,
                new Index()
                        .on("paymentStatus", Sort.Direction.ASC)
                        .on("dueDate", Sort.Direction.ASC)
                        .named("idx_bill_overdue"));

        // 8. Compound index for user + payment status
        ensureIndexSafely(billIndexOps,
                new Index()
                        .on("createdBy", Sort.Direction.ASC)
                        .on("paymentStatus", Sort.Direction.ASC)
                        .named("idx_bill_user_payment"));

        logger.debug("Processed indexes on bills collection");
    }

    /**
     * Farmer Collection Indexes
     */
    private void createFarmerIndexes(MongoTemplate mongoTemplate) {
        IndexOperations farmerIndexOps = mongoTemplate.indexOps(Farmer.class);

        // 1. Unique index on mobile number
        ensureIndexSafely(farmerIndexOps,
                new Index()
                        .on("mobileNumber", Sort.Direction.ASC)
                        .unique()
                        .named("idx_farmer_mobile_unique"));

        // 2. Index for user's farmers
        ensureIndexSafely(farmerIndexOps,
                new Index()
                        .on("createdBy", Sort.Direction.ASC)
                        .named("idx_farmer_user"));

        // 3. Compound index for user's farmers sorted by name
        ensureIndexSafely(farmerIndexOps,
                new Index()
                        .on("createdBy", Sort.Direction.ASC)
                        .on("name", Sort.Direction.ASC)
                        .named("idx_farmer_user_name"));

        // 4. Text index for farmer name search
        ensureIndexSafely(farmerIndexOps,
                new Index()
                        .on("name", Sort.Direction.ASC)
                        .named("idx_farmer_name"));

        logger.debug("Processed indexes on farmers collection");
    }

    /**
     * User Collection Indexes
     */
    private void createUserIndexes(MongoTemplate mongoTemplate) {
        IndexOperations userIndexOps = mongoTemplate.indexOps(User.class);

        // 1. Unique index on mobile number
        ensureIndexSafely(userIndexOps,
                new Index()
                        .on("mobileNumber", Sort.Direction.ASC)
                        .unique()
                        .named("idx_user_mobile_unique"));

        // 2. Unique index on email
        ensureIndexSafely(userIndexOps,
                new Index()
                        .on("email", Sort.Direction.ASC)
                        .unique()
                        .named("idx_user_email_unique"));

        logger.debug("Processed indexes on users collection");
    }

    /**
     * RefreshToken Collection Indexes
     */
    private void createRefreshTokenIndexes(MongoTemplate mongoTemplate) {
        IndexOperations tokenIndexOps = mongoTemplate.indexOps(RefreshToken.class);

        // 1. Unique index on token
        ensureIndexSafely(tokenIndexOps,
                new Index()
                        .on("token", Sort.Direction.ASC)
                        .unique()
                        .named("idx_token_unique"));

        // 2. Compound index for user's active tokens
        ensureIndexSafely(tokenIndexOps,
                new Index()
                        .on("userId", Sort.Direction.ASC)
                        .on("revoked", Sort.Direction.ASC)
                        .named("idx_token_user_active"));

        logger.debug("Processed indexes on refresh_tokens collection");
    }
}
