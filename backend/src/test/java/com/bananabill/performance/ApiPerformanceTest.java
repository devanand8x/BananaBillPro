package com.bananabill.performance;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Performance Tests - Response Time Verification
 * Tests that API endpoints respond within acceptable time limits
 * 
 * Note: For load/stress testing, use k6 scripts in /performance folder
 */
@SpringBootTest
@AutoConfigureMockMvc
class ApiPerformanceTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @WithMockUser(username = "testuser", roles = { "USER" })
    void recentBills_ResponseTime_ShouldBeAcceptable() throws Exception {
        long startTime = System.currentTimeMillis();

        mockMvc.perform(get("/bills/recent").param("limit", "10"))
                .andExpect(status().isOk());

        long duration = System.currentTimeMillis() - startTime;
        assertTrue(duration < 1000, "Response time should be under 1000ms, was: " + duration + "ms");
    }

    @Test
    @WithMockUser(username = "testuser", roles = { "USER" })
    void todayStats_ResponseTime_ShouldBeAcceptable() throws Exception {
        long startTime = System.currentTimeMillis();

        mockMvc.perform(get("/bills/stats/today"))
                .andExpect(status().isOk());

        long duration = System.currentTimeMillis() - startTime;
        assertTrue(duration < 500, "Response time should be under 500ms, was: " + duration + "ms");
    }

    @Test
    @WithMockUser(username = "testuser", roles = { "USER" })
    void farmers_ResponseTime_ShouldBeAcceptable() throws Exception {
        long startTime = System.currentTimeMillis();

        mockMvc.perform(get("/farmers"))
                .andExpect(status().isOk());

        long duration = System.currentTimeMillis() - startTime;
        assertTrue(duration < 1000, "Response time should be under 1000ms, was: " + duration + "ms");
    }

    @Test
    void healthEndpoint_ResponseTime_ShouldBeFast() throws Exception {
        long startTime = System.currentTimeMillis();

        mockMvc.perform(get("/actuator/health"))
                .andExpect(status().isOk());

        long duration = System.currentTimeMillis() - startTime;
        assertTrue(duration < 500, "Health check should be under 500ms, was: " + duration + "ms");
    }

    @Test
    @WithMockUser(username = "testuser", roles = { "USER" })
    void averageResponseTime_ShouldBeAcceptable() throws Exception {
        int iterations = 5;
        long totalTime = 0;

        for (int i = 0; i < iterations; i++) {
            long start = System.currentTimeMillis();
            mockMvc.perform(get("/bills/stats/today"))
                    .andExpect(status().isOk());
            totalTime += System.currentTimeMillis() - start;
        }

        double avgTime = (double) totalTime / iterations;
        assertTrue(avgTime < 500, "Average response time should be under 500ms, was: " + avgTime + "ms");
    }
}
