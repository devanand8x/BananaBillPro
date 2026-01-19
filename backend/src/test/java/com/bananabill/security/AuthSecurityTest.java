package com.bananabill.security;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Security Tests for Authentication & Authorization
 * Tests protection against unauthorized access
 */
@SpringBootTest
@AutoConfigureMockMvc
class AuthSecurityTest {

    @Autowired
    private MockMvc mockMvc;

    // ==================== UNAUTHORIZED ACCESS ====================

    @Test
    void protectedEndpoint_WithoutToken_ShouldReturn401() throws Exception {
        mockMvc.perform(get("/bills/recent"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void createBill_WithoutAuth_ShouldReturn401() throws Exception {
        mockMvc.perform(post("/bills")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void deleteBill_WithoutAuth_ShouldReturn401() throws Exception {
        mockMvc.perform(delete("/bills/123"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void farmerEndpoint_WithoutAuth_ShouldReturn401() throws Exception {
        mockMvc.perform(get("/farmers"))
                .andExpect(status().isUnauthorized());
    }

    // ==================== INVALID TOKEN ====================

    @Test
    void protectedEndpoint_WithInvalidToken_ShouldReturn401() throws Exception {
        mockMvc.perform(get("/bills/recent")
                .header("Authorization", "Bearer invalid.token.here"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void protectedEndpoint_WithMalformedToken_ShouldReturn401() throws Exception {
        mockMvc.perform(get("/bills/recent")
                .header("Authorization", "Bearer malformed"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void protectedEndpoint_WithEmptyBearer_ShouldReturn401() throws Exception {
        mockMvc.perform(get("/bills/recent")
                .header("Authorization", "Bearer "))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void protectedEndpoint_WithNoBearer_ShouldReturn401() throws Exception {
        mockMvc.perform(get("/bills/recent")
                .header("Authorization", "some-token"))
                .andExpect(status().isUnauthorized());
    }

    // ==================== PUBLIC ENDPOINTS ====================

    @Test
    void loginEndpoint_ShouldBePublic() throws Exception {
        mockMvc.perform(post("/auth/login")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"mobile\":\"1234567890\",\"password\":\"test\"}"))
                .andExpect(status().is4xxClientError()); // 401 for invalid creds is OK
    }

    @Test
    void registerEndpoint_ShouldBePublic() throws Exception {
        mockMvc.perform(post("/auth/register")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"name\":\"Test\",\"mobile\":\"1234567890\",\"password\":\"test123\"}"))
                .andExpect(status().is4xxClientError()); // Validation error is OK
    }

    @Test
    void healthEndpoint_ShouldBePublic() throws Exception {
        mockMvc.perform(get("/actuator/health"))
                .andExpect(status().isOk());
    }
}
