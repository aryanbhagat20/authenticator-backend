package com.aryanhagat.authenticator.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;

// No @ExtendWith needed — JwtService has no dependencies to mock
class JwtServiceTest {

    private JwtService jwtService;

    @BeforeEach
    void setUp() {
        jwtService = new JwtService();

        // JwtService reads jwt.secret and jwt.expiration from application.properties
        // using @Value. In tests, Spring context isn't loaded, so we inject
        // the values manually using ReflectionTestUtils.
        // This sets private fields directly without needing a setter.
        ReflectionTestUtils.setField(
                jwtService,
                "secretHex",
                "404E635266556A586E3272357538782F413F4428472B4B6250645367566B5970"
        );
        ReflectionTestUtils.setField(
                jwtService,
                "expirationMs",
                86400000L
        );
    }

    @Test
    void generateToken_ReturnsNonNullToken() {
        // ACT
        String token = jwtService.generateToken("test@gmail.com");

        // ASSERT
        assertThat(token).isNotNull();
        assertThat(token).isNotBlank();
    }

    @Test
    void generateToken_ProducesThreePartJwt() {
        // A valid JWT always has exactly 3 parts separated by dots
        String token = jwtService.generateToken("test@gmail.com");
        String[] parts = token.split("\\.");

        assertThat(parts).hasSize(3);
    }

    @Test
    void extractEmail_ReturnsCorrectEmail() {
        // ARRANGE
        String email = "test@gmail.com";
        String token = jwtService.generateToken(email);

        // ACT
        String extractedEmail = jwtService.extractEmail(token);

        // ASSERT
        assertThat(extractedEmail).isEqualTo(email);
    }

    @Test
    void isTokenValid_ReturnsTrueForValidToken() {
        // ARRANGE
        String token = jwtService.generateToken("test@gmail.com");

        // ACT + ASSERT
        assertThat(jwtService.isTokenValid(token)).isTrue();
    }

    @Test
    void isTokenValid_ReturnsFalseForGarbageToken() {
        assertThat(jwtService.isTokenValid("this.is.garbage")).isFalse();
    }

    @Test
    void isTokenValid_ReturnsFalseForTamperedToken() {
        // ARRANGE — generate a real token then tamper with its payload
        String token = jwtService.generateToken("test@gmail.com");
        String[] parts = token.split("\\.");

        // Replace the payload (middle part) with a different base64 string
        String tamperedToken = parts[0] + ".TAMPERED_PAYLOAD." + parts[2];

        // ASSERT — signature won't match the tampered payload
        assertThat(jwtService.isTokenValid(tamperedToken)).isFalse();
    }

    @Test
    void isTokenValid_ReturnsFalseForExpiredToken() {
        // ARRANGE — create a JwtService with 0ms expiration
        JwtService expiredJwtService = new JwtService();
        ReflectionTestUtils.setField(
                expiredJwtService,
                "secretHex",
                "404E635266556A586E3272357538782F413F4428472B4B6250645367566B5970"
        );
        ReflectionTestUtils.setField(
                expiredJwtService,
                "expirationMs",
                0L // expires immediately
        );

        String token = expiredJwtService.generateToken("test@gmail.com");

        // ASSERT — token should already be expired
        assertThat(expiredJwtService.isTokenValid(token)).isFalse();
    }
}