package com.aryanhagat.authenticator.service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.HexFormat;

@Service
public class JwtService {

    // Reads jwt.secret from application.properties
    @Value("${jwt.secret}")
    private String secretHex;

    // Reads jwt.expiration from application.properties
    @Value("${jwt.expiration}")
    private long expirationMs;

    // Converts the hex string from properties into an actual cryptographic key
    private SecretKey getSigningKey() {
        byte[] keyBytes = HexFormat.of().parseHex(secretHex);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    // Generate a JWT token for a given email
    public String generateToken(String email) {
        return Jwts.builder()
                .subject(email)           // "sub" claim — who this token is for
                .issuedAt(new Date())     // "iat" claim — when it was issued
                .expiration(new Date(System.currentTimeMillis() + expirationMs)) // "exp" claim
                .signWith(getSigningKey()) // signs with HS256 by default
                .compact();               // builds the final token string
    }

    // Extract the email (subject) from a token
    public String extractEmail(String token) {
        return extractAllClaims(token).getSubject();
    }

    // Check if a token is still valid
    public boolean isTokenValid(String token) {
        try {
            Claims claims = extractAllClaims(token);
            // If expiration date is after right now, token is valid
            return claims.getExpiration().after(new Date());
        } catch (Exception e) {
            // Any exception means token is malformed, expired, or tampered
            return false;
        }
    }

    // Parse the token and get all claims from payload
    private Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey()) // uses same key to verify signature
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}