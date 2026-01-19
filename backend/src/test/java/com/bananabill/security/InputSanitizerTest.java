package com.bananabill.security;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class InputSanitizerTest {

    @Test
    void sanitize_NullInput_ShouldReturnNull() {
        assertNull(InputSanitizer.sanitize(null));
    }

    @Test
    void sanitize_EmptyInput_ShouldReturnEmpty() {
        assertEquals("", InputSanitizer.sanitize(""));
    }

    @Test
    void sanitize_PlainText_ShouldReturnSame() {
        assertEquals("Hello World", InputSanitizer.sanitize("Hello World"));
    }

    @Test
    void sanitize_ScriptTag_ShouldRemove() {
        String input = "<script>alert('xss')</script>Hello";
        String result = InputSanitizer.sanitize(input);
        assertFalse(result.contains("<script"));
        assertFalse(result.contains("alert"));
    }

    @Test
    void sanitize_HtmlTags_ShouldRemove() {
        String input = "<div><b>Bold</b></div>";
        String result = InputSanitizer.sanitize(input);
        assertFalse(result.contains("<"));
        assertFalse(result.contains(">"));
    }

    @Test
    void sanitize_JavascriptProtocol_ShouldRemove() {
        String input = "javascript:alert(1)";
        String result = InputSanitizer.sanitize(input);
        assertFalse(result.toLowerCase().contains("javascript:"));
    }

    @Test
    void sanitize_OnEventHandler_ShouldRemove() {
        String input = "onclick=attack()";
        String result = InputSanitizer.sanitize(input);
        assertFalse(result.contains("onclick="));
    }

    @Test
    void sanitizeWithMaxLength_ShouldTruncate() {
        String input = "This is a very long text that should be truncated";
        String result = InputSanitizer.sanitize(input, 10);
        assertEquals(10, result.length());
    }

    @Test
    void containsXss_WithScript_ShouldReturnTrue() {
        assertTrue(InputSanitizer.containsXss("<script>alert(1)</script>"));
    }

    @Test
    void containsXss_WithPlainText_ShouldReturnFalse() {
        assertFalse(InputSanitizer.containsXss("Normal text"));
    }

    @Test
    void containsXss_WithJavascript_ShouldReturnTrue() {
        assertTrue(InputSanitizer.containsXss("javascript:void(0)"));
    }

    @Test
    void containsXss_NullInput_ShouldReturnFalse() {
        assertFalse(InputSanitizer.containsXss(null));
    }

    @Test
    void sanitizeForLog_ShortInput_ShouldReturnSame() {
        assertEquals("short", InputSanitizer.sanitizeForLog("short", 10));
    }

    @Test
    void sanitizeForLog_LongInput_ShouldMask() {
        String result = InputSanitizer.sanitizeForLog("1234567890ABCDEF", 4);
        assertEquals("1234***", result);
    }

    @Test
    void escapeHtml_NullInput_ShouldReturnNull() {
        assertNull(InputSanitizer.escapeHtml(null));
    }

    @Test
    void escapeHtml_SpecialChars_ShouldEscape() {
        String input = "<div class=\"test\">";
        String result = InputSanitizer.escapeHtml(input);
        assertTrue(result.contains("&lt;"));
        assertTrue(result.contains("&gt;"));
        assertTrue(result.contains("&quot;"));
    }

    @Test
    void escapeHtml_Ampersand_ShouldEscape() {
        String result = InputSanitizer.escapeHtml("A & B");
        assertTrue(result.contains("&amp;"));
    }
}
