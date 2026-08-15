package com.example.demo.util;

import com.example.demo.exception.InvalidUrlException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class UrlSafetyGuardTest {

    @Test
    void rejectsNullOrBlank() {
        assertThrows(InvalidUrlException.class, () -> UrlSafetyGuard.normalizeAndValidate(null));
        assertThrows(InvalidUrlException.class, () -> UrlSafetyGuard.normalizeAndValidate("   "));
    }

    @Test
    void rejectsJavascriptScheme() {
        assertThrows(InvalidUrlException.class, () -> UrlSafetyGuard.normalizeAndValidate("javascript:alert(1)"));
    }

    @Test
    void rejectsLocalhost() {
        assertThrows(InvalidUrlException.class, () -> UrlSafetyGuard.normalizeAndValidate("http://localhost/"));
    }

    @Test
    void stripsFragment() {
        String r = UrlSafetyGuard.normalizeAndValidate("https://example.com/path#frag");
        assertFalse(r.contains("#"));
        assertTrue(r.startsWith("https://example.com"));
    }

    @Test
    void rejectsControlChars() {
        assertThrows(InvalidUrlException.class, () -> UrlSafetyGuard.normalizeAndValidate("https://example.com/%0a"));
    }

    @Test
    void acceptsNormalUrl() {
        String r = UrlSafetyGuard.normalizeAndValidate("https://example.org/x?q=1");
        assertEquals("https://example.org/x?q=1", r);
    }
}
