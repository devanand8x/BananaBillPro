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
 * Security Tests for Input Validation
 * Tests validation of all user inputs
 */
@SpringBootTest
@AutoConfigureMockMvc
class ValidationSecurityTest {

    @Autowired
    private MockMvc mockMvc;

    // ==================== LOGIN VALIDATION ====================

    @Test
    void login_WithEmptyMobile_ShouldFail() throws Exception {
        mockMvc.perform(post("/auth/login")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"mobile\":\"\",\"password\":\"test123\"}"))
                .andExpect(status().is4xxClientError());
    }

    @Test
    void login_WithShortMobile_ShouldFail() throws Exception {
        mockMvc.perform(post("/auth/login")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"mobile\":\"123\",\"password\":\"test123\"}"))
                .andExpect(status().is4xxClientError());
    }

    @Test
    void login_WithEmptyPassword_ShouldFail() throws Exception {
        mockMvc.perform(post("/auth/login")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"mobile\":\"9876543210\",\"password\":\"\"}"))
                .andExpect(status().is4xxClientError());
    }

    // ==================== REGISTRATION VALIDATION ====================

    @Test
    void register_WithEmptyName_ShouldFail() throws Exception {
        mockMvc.perform(post("/auth/register")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"name\":\"\",\"mobile\":\"9876543210\",\"password\":\"Test@1234\"}"))
                .andExpect(status().is4xxClientError());
    }

    @Test
    void register_WithInvalidMobile_ShouldFail() throws Exception {
        mockMvc.perform(post("/auth/register")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"name\":\"Test\",\"mobile\":\"invalid\",\"password\":\"Test@1234\"}"))
                .andExpect(status().is4xxClientError());
    }

    @Test
    void register_WithWeakPassword_ShouldFail() throws Exception {
        mockMvc.perform(post("/auth/register")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"name\":\"Test\",\"mobile\":\"9876543210\",\"password\":\"123\"}"))
                .andExpect(status().is4xxClientError());
    }

    // ==================== NUMERIC VALIDATION ====================

    @Test
    void login_WithNonNumericMobile_ShouldFail() throws Exception {
        mockMvc.perform(post("/auth/login")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"mobile\":\"abcdefghij\",\"password\":\"test123\"}"))
                .andExpect(status().is4xxClientError());
    }

    // ==================== LENGTH VALIDATION ====================

    @Test
    void register_WithTooLongName_ShouldFail() throws Exception {
        String longName = "A".repeat(500);
        mockMvc.perform(post("/auth/register")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"name\":\"" + longName + "\",\"mobile\":\"9876543210\",\"password\":\"Test@1234\"}"))
                .andExpect(status().is4xxClientError());
    }
}
