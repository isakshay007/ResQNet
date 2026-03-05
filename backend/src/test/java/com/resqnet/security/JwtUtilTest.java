package com.resqnet.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class JwtUtilTest {

    private static final String SECRET = "resqnet_super_long_secret_key_for_jwt_which_is_at_least_32_chars_long_12345";

    private JwtUtil jwtUtil;

    @BeforeEach
    void setUp() {
        jwtUtil = new JwtUtil(SECRET, 36_000_000L);
    }

    @Test
    void generateToken_andExtractEmail_returnsCorrectSubject() {
        String token = jwtUtil.generateToken("user@example.com", "REPORTER");

        assertNotNull(token);
        assertEquals("user@example.com", jwtUtil.extractEmail(token));
    }

    @Test
    void generateToken_andExtractRole_returnsCorrectRole() {
        String token = jwtUtil.generateToken("user@example.com", "ADMIN");

        assertEquals("ADMIN", jwtUtil.extractRole(token));
    }

    @Test
    void validateToken_withMatchingEmail_returnsTrue() {
        String token = jwtUtil.generateToken("user@example.com", "REPORTER");

        assertTrue(jwtUtil.validateToken(token, "user@example.com"));
    }

    @Test
    void validateToken_withMismatchedEmail_returnsFalse() {
        String token = jwtUtil.generateToken("user@example.com", "REPORTER");

        assertFalse(jwtUtil.validateToken(token, "other@example.com"));
    }

    @Test
    void expiredToken_throwsExceptionOnParse() throws InterruptedException {
        JwtUtil shortLivedUtil = new JwtUtil(SECRET, 1L);
        String token = shortLivedUtil.generateToken("user@example.com", "REPORTER");

        Thread.sleep(50);

        assertThrows(Exception.class, () -> shortLivedUtil.validateToken(token, "user@example.com"));
    }
}
