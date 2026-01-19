package com.bananabill.integration;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration Tests for API Endpoints
 * Tests API contracts and endpoint availability
 */
@SpringBootTest
@AutoConfigureMockMvc
class ApiIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    // ==================== AUTH ENDPOINTS ====================

    @Test
    void loginEndpoint_ShouldBeAccessible() throws Exception {
        mockMvc.perform(post("/auth/login")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"mobile\":\"9876543210\",\"password\":\"test123\"}"))
                .andExpect(status().is4xxClientError()); // 401 for invalid creds is OK
    }

    @Test
    void registerEndpoint_ShouldBeAccessible() throws Exception {
        mockMvc.perform(post("/auth/register")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"name\":\"Test\",\"mobile\":\"1234567890\",\"password\":\"test123\"}"))
                .andExpect(status().is4xxClientError()); // Validation error is OK
    }

    // ==================== PROTECTED ENDPOINTS ====================

    @Test
    void protectedEndpoint_WithoutAuth_ShouldReturn401() throws Exception {
        mockMvc.perform(get("/bills/recent"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(username = "testuser", roles = { "USER" })
    void recentBills_WithAuth_ShouldReturn200() throws Exception {
        mockMvc.perform(get("/bills/recent")
                .param("limit", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @WithMockUser(username = "testuser", roles = { "USER" })
    void getTodayStats_WithAuth_ShouldReturn200() throws Exception {
        mockMvc.perform(get("/bills/stats/today"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @WithMockUser(username = "testuser", roles = { "USER" })
    void getAllFarmers_WithAuth_ShouldReturn200() throws Exception {
        mockMvc.perform(get("/farmers"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    // ==================== HEALTH CHECK ====================

    @Test
    void healthEndpoint_ShouldReturn200() throws Exception {
        mockMvc.perform(get("/actuator/health"))
                .andExpect(status().isOk());
    }
}
