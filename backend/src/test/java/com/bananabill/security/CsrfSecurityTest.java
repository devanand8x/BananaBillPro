package com.bananabill.security;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Security Tests for API Access Control
 * Tests that protected endpoints require authentication
 */
@SpringBootTest
@AutoConfigureMockMvc
class CsrfSecurityTest {

    @Autowired
    private MockMvc mockMvc;

    // ==================== GET REQUESTS (NO AUTH) ====================

    @Test
    void getRecentBills_WithoutAuth_ShouldReturn401() throws Exception {
        mockMvc.perform(get("/bills/recent"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void getAllFarmers_WithoutAuth_ShouldReturn401() throws Exception {
        mockMvc.perform(get("/farmers"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void getTodayStats_WithoutAuth_ShouldReturn401() throws Exception {
        mockMvc.perform(get("/bills/stats/today"))
                .andExpect(status().isUnauthorized());
    }

    // ==================== WITH AUTH (SHOULD WORK) ====================

    @Test
    @WithMockUser(username = "testuser", roles = { "USER" })
    void getRecentBills_WithAuth_ShouldReturn200() throws Exception {
        mockMvc.perform(get("/bills/recent")
                .param("limit", "10"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(username = "testuser", roles = { "USER" })
    void getAllFarmers_WithAuth_ShouldReturn200() throws Exception {
        mockMvc.perform(get("/farmers"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(username = "testuser", roles = { "USER" })
    void getTodayStats_WithAuth_ShouldReturn200() throws Exception {
        mockMvc.perform(get("/bills/stats/today"))
                .andExpect(status().isOk());
    }
}
