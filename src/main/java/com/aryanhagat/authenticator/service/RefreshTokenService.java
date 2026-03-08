package com.aryanhagat.authenticator.service;

import com.aryanhagat.authenticator.entity.RefreshToken;
import com.aryanhagat.authenticator.entity.User;
import com.aryanhagat.authenticator.exception.InvalidRefreshTokenException;
import com.aryanhagat.authenticator.repository.RefreshTokenRepository;
import com.aryanhagat.authenticator.repository.UserRepository;
import com.aryanhagat.authenticator.exception.UserNotFoundException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

@Service
public class RefreshTokenService {

    private final RefreshTokenRepository refreshTokenRepository;
    private final UserRepository userRepository;

    @Value("${jwt.refresh-expiration}")
    private long refreshExpirationMs;

    public RefreshTokenService(RefreshTokenRepository refreshTokenRepository,
                               UserRepository userRepository) {
        this.refreshTokenRepository = refreshTokenRepository;
        this.userRepository = userRepository;
    }


    // Create a new refresh token for a user
    // Called on login and after OTP verification
    @Transactional
    public RefreshToken createRefreshToken(String email) {

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        // Delete any existing refresh token for this user
        refreshTokenRepository.deleteByUser(user);

        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setUser(user);

        // UUID is a random 128-bit value — essentially impossible to guess
        refreshToken.setToken(UUID.randomUUID().toString());

        // Set expiry to now + 7 days
        refreshToken.setExpiryDate(
                Instant.now().plusMillis(refreshExpirationMs)
        );

        return refreshTokenRepository.save(refreshToken);
    }

    // Verify a refresh token is valid and not expired
    public RefreshToken verifyRefreshToken(String token) {

        RefreshToken refreshToken = refreshTokenRepository.findByToken(token)
                .orElseThrow(() ->
                        new InvalidRefreshTokenException("Refresh token not found")
                );

        // Check if expired
        if (refreshToken.getExpiryDate().isBefore(Instant.now())) {
            // Clean up expired token from DB
            refreshTokenRepository.delete(refreshToken);
            throw new InvalidRefreshTokenException(
                    "Refresh token expired. Please log in again."
            );
        }

        return refreshToken;
    }


    // Delete refresh token on logout
    @Transactional
    public void deleteByToken(String token) {
        refreshTokenRepository.findByToken(token)
                .ifPresent(refreshTokenRepository::delete);
    }
}