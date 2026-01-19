package com.bananabill.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

/**
 * Async Configuration for non-blocking operations
 * 
 * Used for:
 * - WhatsApp message sending (external API calls)
 * - Background report generation
 * - Notification dispatch
 */
@Configuration
@EnableAsync
public class AsyncConfig {

    @Bean(name = "whatsappExecutor")
    public Executor whatsappExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(2);
        executor.setMaxPoolSize(5);
        executor.setQueueCapacity(100);
        executor.setThreadNamePrefix("whatsapp-");
        executor.setKeepAliveSeconds(60);
        executor.initialize();
        return executor;
    }

    @Bean(name = "reportExecutor")
    public Executor reportExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(2);
        executor.setMaxPoolSize(4);
        executor.setQueueCapacity(50);
        executor.setThreadNamePrefix("report-");
        executor.initialize();
        return executor;
    }
}
