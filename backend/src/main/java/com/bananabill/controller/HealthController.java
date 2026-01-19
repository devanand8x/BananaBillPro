package com.bananabill.controller;

import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.RuntimeMXBean;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Health and Metrics Controller for monitoring
 * Essential for load balancer health checks
 */
@RestController
@RequestMapping("")
public class HealthController {

    private final MongoTemplate mongoTemplate;
    private final com.bananabill.service.ImageUploadService imageUploadService;
    private static final LocalDateTime START_TIME = LocalDateTime.now();

    public HealthController(MongoTemplate mongoTemplate, com.bananabill.service.ImageUploadService imageUploadService) {
        this.mongoTemplate = mongoTemplate;
        this.imageUploadService = imageUploadService;
    }

    /**
     * Debug endpoint to check Cloudinary config
     * GET /api/debug/cloudinary
     */
    @GetMapping("/debug/cloudinary")
    public ResponseEntity<Map<String, Object>> debugCloudinary() {
        Map<String, Object> debug = new HashMap<>();
        debug.put("configured", imageUploadService.isConfigured());
        debug.put("timestamp", LocalDateTime.now());
        return ResponseEntity.ok(debug);
    }

    /**
     * Simple health check for load balancer
     * GET /api/health
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> health() {
        Map<String, Object> health = new HashMap<>();
        health.put("status", "UP");
        health.put("message", "Banana Bill Backend is running!");
        health.put("timestamp", LocalDateTime.now());

        return ResponseEntity.ok(health);
    }

    /**
     * Detailed health check with component status
     * GET /api/health/detailed
     */
    @GetMapping("/health/detailed")
    public ResponseEntity<Map<String, Object>> detailedHealth() {
        Map<String, Object> health = new HashMap<>();

        // Overall status
        health.put("status", "UP");
        health.put("timestamp", LocalDateTime.now());

        // Components
        Map<String, Object> components = new HashMap<>();

        // Database check
        try {
            mongoTemplate.executeCommand("{ ping: 1 }");
            components.put("mongodb", Map.of("status", "UP"));
        } catch (Exception e) {
            components.put("mongodb", Map.of("status", "DOWN", "error", e.getMessage()));
        }

        // Memory info
        MemoryMXBean memoryBean = ManagementFactory.getMemoryMXBean();
        long heapUsed = memoryBean.getHeapMemoryUsage().getUsed() / (1024 * 1024);
        long heapMax = memoryBean.getHeapMemoryUsage().getMax() / (1024 * 1024);
        components.put("memory", Map.of(
                "used", heapUsed + "MB",
                "max", heapMax + "MB",
                "percentage", (heapUsed * 100 / heapMax) + "%"));

        // Uptime
        RuntimeMXBean runtimeBean = ManagementFactory.getRuntimeMXBean();
        long uptimeMs = runtimeBean.getUptime();
        Duration uptime = Duration.ofMillis(uptimeMs);
        components.put("uptime", Map.of(
                "started", START_TIME,
                "duration", String.format("%d days, %d hours, %d minutes",
                        uptime.toDays(),
                        uptime.toHoursPart(),
                        uptime.toMinutesPart())));

        health.put("components", components);

        return ResponseEntity.ok(health);
    }

    /**
     * Readiness check for Kubernetes/Load Balancer
     * GET /api/health/ready
     */
    @GetMapping("/health/ready")
    public ResponseEntity<Map<String, String>> readiness() {
        try {
            // Check if database is accessible
            mongoTemplate.executeCommand("{ ping: 1 }");
            return ResponseEntity.ok(Map.of("status", "READY"));
        } catch (Exception e) {
            return ResponseEntity.status(503).body(Map.of(
                    "status", "NOT_READY",
                    "reason", "Database unavailable"));
        }
    }

    /**
     * Liveness check for Kubernetes
     * GET /api/health/live
     */
    @GetMapping("/health/live")
    public ResponseEntity<Map<String, String>> liveness() {
        return ResponseEntity.ok(Map.of("status", "ALIVE"));
    }
}
