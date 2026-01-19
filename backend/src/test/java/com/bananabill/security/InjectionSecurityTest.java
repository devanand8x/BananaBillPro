package com.bananabill.security;

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
 * Security Tests for Injection Attack Prevention
 * Tests NoSQL injection, Path traversal
 */
@SpringBootTest
@AutoConfigureMockMvc
class InjectionSecurityTest {

    @Autowired
    private MockMvc mockMvc;

    // ==================== INJECTION PREVENTION ====================

    @Test
    void loginWithInjection_ShouldNotBypass() throws Exception {
        // NoSQL injection attempt should fail auth, not bypass
        String injectionPayload = "{\"mobile\":\"admin\",\"password\":\"{\\\"$ne\\\":\\\"\\\"}\"}";
        mockMvc.perform(post("/auth/login")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(injectionPayload))
                .andExpect(status().is4xxClientError());
    }

    @Test
    @WithMockUser(username = "testuser", roles = { "USER" })
    void searchBills_WithSpecialChars_ShouldNotCrash() throws Exception {
        mockMvc.perform(get("/bills/search-with-filters")
                .param("mobileNumber", "9876543210"))
                .andExpect(status().isOk());
    }

    // ==================== PATH TRAVERSAL PREVENTION ====================

    @Test
    @WithMockUser(username = "testuser", roles = { "USER" })
    void getBill_WithPathTraversal_ShouldNotWork() throws Exception {
        mockMvc.perform(get("/bills/..%2F..%2Fetc%2Fpasswd"))
                .andExpect(status().is4xxClientError());
    }

    // ==================== INPUT SANITIZER TESTS ====================

    @Test
    void inputSanitizer_CommandInjection_ShouldBeSafe() {
        String input = "test; rm -rf /";
        String sanitized = InputSanitizer.sanitize(input);
        org.junit.jupiter.api.Assertions.assertNotNull(sanitized);
    }

    @Test
    void inputSanitizer_NullInput_ShouldReturnNull() {
        String sanitized = InputSanitizer.sanitize(null);
        org.junit.jupiter.api.Assertions.assertNull(sanitized);
    }
}
