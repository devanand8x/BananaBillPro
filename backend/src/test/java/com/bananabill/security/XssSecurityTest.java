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
 * Security Tests for XSS (Cross-Site Scripting) Prevention
 * Tests InputSanitizer utility functions
 */
@SpringBootTest
@AutoConfigureMockMvc
class XssSecurityTest {

    @Autowired
    private MockMvc mockMvc;

    // ==================== INPUT SANITIZER UNIT TESTS ====================

    @Test
    void inputSanitizer_ScriptTag_ShouldBeRemoved() {
        String input = "<script>alert('xss')</script>Hello";
        String result = InputSanitizer.sanitize(input);
        org.junit.jupiter.api.Assertions.assertFalse(result.contains("<script"));
    }

    @Test
    void inputSanitizer_JavascriptProtocol_ShouldBeRemoved() {
        String input = "javascript:alert(1)";
        String result = InputSanitizer.sanitize(input);
        org.junit.jupiter.api.Assertions.assertFalse(result.toLowerCase().contains("javascript:"));
    }

    @Test
    void inputSanitizer_OnEvent_ShouldBeRemoved() {
        String input = "onclick=attack()";
        String result = InputSanitizer.sanitize(input);
        org.junit.jupiter.api.Assertions.assertFalse(result.contains("onclick="));
    }

    @Test
    void inputSanitizer_HtmlTags_ShouldBeRemoved() {
        String input = "<div><b>Bold</b></div>";
        String result = InputSanitizer.sanitize(input);
        org.junit.jupiter.api.Assertions.assertFalse(result.contains("<"));
    }

    @Test
    void inputSanitizer_SafeText_ShouldRemainUnchanged() {
        String input = "Hello World";
        String result = InputSanitizer.sanitize(input);
        org.junit.jupiter.api.Assertions.assertEquals("Hello World", result);
    }
}
