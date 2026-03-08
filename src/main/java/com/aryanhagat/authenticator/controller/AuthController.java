package com.aryanhagat.authenticator.controller;

import com.aryanhagat.authenticator.dto.*;
import com.aryanhagat.authenticator.entity.RefreshToken;
import com.aryanhagat.authenticator.service.AuthService;
import com.aryanhagat.authenticator.service.JwtService;
import com.aryanhagat.authenticator.service.RefreshTokenService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthService authService;
    private final RefreshTokenService refreshTokenService;
    private final JwtService jwtService;

    public AuthController(AuthService authService,
                          RefreshTokenService refreshTokenService,
                          JwtService jwtService) {
        this.authService = authService;
        this.refreshTokenService = refreshTokenService;
        this.jwtService = jwtService;
    }

    @PostMapping("/signup")
    public ResponseEntity<String> signup(@Valid @RequestBody SignupRequest request) {
        authService.signup(request);
        return ResponseEntity.ok("User registered successfully");
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(
            @Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }

    @PostMapping("/login/2fa")
    public ResponseEntity<LoginResponse> verifyLoginOtp(
            @Valid @RequestBody LoginOtpRequest request) {
        return ResponseEntity.ok(
                authService.verifyLoginOtp(
                        request.getEmail(),
                        request.getOtp()
                )
        );
    }

    @GetMapping("/me")
    public ResponseEntity<String> getCurrentUser(Authentication authentication) {
        return ResponseEntity.ok("Logged in as: " + authentication.getName());
    }

    // Exchange refresh token for new access token
    @PostMapping("/refresh")
    public ResponseEntity<RefreshTokenResponse> refreshToken(
            @Valid @RequestBody RefreshTokenRequest request) {

        RefreshToken refreshToken =
                refreshTokenService.verifyRefreshToken(request.getRefreshToken());

        String email = refreshToken.getUser().getEmail();

        String newAccessToken = jwtService.generateToken(email);

        return ResponseEntity.ok(
                new RefreshTokenResponse(newAccessToken, request.getRefreshToken())
        );
    }

    // Logout — invalidate refresh token
    @PostMapping("/logout")
    public ResponseEntity<String> logout(
            @Valid @RequestBody RefreshTokenRequest request) {

        // Delete the refresh token from DB
        refreshTokenService.deleteByToken(request.getRefreshToken());

        return ResponseEntity.ok("Logged out successfully");
    }
}