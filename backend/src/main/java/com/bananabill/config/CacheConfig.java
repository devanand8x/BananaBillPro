package com.bananabill.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.time.Duration;

/**
 * Caching configuration for improved performance
 * Uses Redis for production, in-memory for development
 */
@Configuration
@EnableCaching
public class CacheConfig {

        /**
         * Cache names used in the application
         */
        public static final String BILL_STATS = "billStats";
        public static final String FARMER_CACHE = "farmers";
        public static final String RECENT_BILLS = "recentBills";

        @Autowired(required = false)
        private RedisConnectionFactory redisConnectionFactory;

        /**
         * Redis Cache Manager for production
         */
        @Bean
        @Primary
        @ConditionalOnProperty(name = "spring.cache.type", havingValue = "redis")
        public CacheManager redisCacheManager() {
                if (redisConnectionFactory == null) {
                        // Fallback if factory is null even if property says redis
                        return new ConcurrentMapCacheManager(BILL_STATS, FARMER_CACHE, RECENT_BILLS);
                }

                RedisCacheConfiguration config = RedisCacheConfiguration.defaultCacheConfig()
                                .entryTtl(Duration.ofHours(1))
                                .serializeKeysWith(RedisSerializationContext.SerializationPair.fromSerializer(
                                                new StringRedisSerializer()))
                                .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(
                                                new GenericJackson2JsonRedisSerializer()))
                                .disableCachingNullValues();

                return RedisCacheManager.builder(redisConnectionFactory)
                                .cacheDefaults(config)
                                .withCacheConfiguration(FARMER_CACHE, config.entryTtl(Duration.ofHours(24)))
                                .withCacheConfiguration(BILL_STATS, config.entryTtl(Duration.ofMinutes(10)))
                                .withCacheConfiguration(RECENT_BILLS, config.entryTtl(Duration.ofMinutes(5)))
                                .transactionAware()
                                .build();
        }

        /**
         * In-memory Cache Manager for development (fallback)
         */
        @Bean
        @ConditionalOnProperty(name = "spring.cache.type", havingValue = "simple", matchIfMissing = true)
        public CacheManager inMemoryCacheManager() {
                return new ConcurrentMapCacheManager(
                                BILL_STATS,
                                FARMER_CACHE,
                                RECENT_BILLS);
        }
}
