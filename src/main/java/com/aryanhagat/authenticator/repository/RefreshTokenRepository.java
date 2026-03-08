package com.aryanhagat.authenticator.repository;

import com.aryanhagat.authenticator.entity.RefreshToken;
import com.aryanhagat.authenticator.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {

    // Used when client sends refresh token to get new access token
    Optional<RefreshToken> findByToken(String token);

    // Used on logout and when issuing a new token (replace old one)
    void deleteByUser(User user);
}