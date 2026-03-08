package com.aryanhagat.authenticator.controller;

import com.aryanhagat.authenticator.dto.*;
import com.aryanhagat.authenticator.entity.RefreshToken;
import com.aryanhagat.authenticator.service.AuthService;
import com.aryanhagat.authenticator.service.JwtService;
import com.aryanhagat.authenticator.service.RefreshTokenService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@Tag(name = "Authentication", description = "Endpoints for signup, login, token management and logout")
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

    @Operation(
            summary = "Register a new user",
            description = "Creates a new user account with email and password. " +
                    "A 2FA secret is automatically generated but 2FA is disabled until explicitly enabled."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "User registered successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid input — email format wrong or password too short"),
            @ApiResponse(responseCode = "409", description = "Email already registered")
    })
    @PostMapping("/signup")
    public ResponseEntity<String> signup(@Valid @RequestBody SignupRequest request) {
        authService.signup(request);
        return ResponseEntity.ok("User registered successfully");
    }

    @Operation(
            summary = "Login with email and password",
            description = "Authenticates the user. If 2FA is enabled, returns otpRequired=true " +
                    "and no token — client must then call POST /auth/login/2fa. " +
                    "If 2FA is not enabled, returns both access and refresh tokens immediately."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Login successful or OTP required"),
            @ApiResponse(responseCode = "400", description = "Invalid input"),
            @ApiResponse(responseCode = "401", description = "Invalid email or password"),
            @ApiResponse(responseCode = "404", description = "User not found"),
            @ApiResponse(responseCode = "429", description = "Too many login attempts — rate limited")
    })
    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(
            @Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }

    @Operation(
            summary = "Complete login with 2FA OTP",
            description = "Second step of login when 2FA is enabled. " +
                    "Submit the 6-digit OTP from your authenticator app. " +
                    "Returns both access and refresh tokens on success."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OTP verified — tokens issued"),
            @ApiResponse(responseCode = "400", description = "Invalid input or OTP format"),
            @ApiResponse(responseCode = "401", description = "Invalid OTP"),
            @ApiResponse(responseCode = "429", description = "Too many attempts — rate limited")
    })
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

    @Operation(
            summary = "Get current authenticated user",
            description = "Returns the email of the currently authenticated user. " +
                    "Requires a valid JWT access token. Use this to verify your token works."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Returns logged in user email"),
            @ApiResponse(responseCode = "401", description = "Invalid or expired access token")
    })
    @SecurityRequirement(name = "Bearer Authentication")
    @GetMapping("/me")
    public ResponseEntity<String> getCurrentUser(Authentication authentication) {
        return ResponseEntity.ok("Logged in as: " + authentication.getName());
    }

    @Operation(
            summary = "Refresh access token",
            description = "Exchange a valid refresh token for a new access token. " +
                    "Use this when your access token expires (after 15 minutes). " +
                    "The refresh token remains valid for 7 days."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "New access token issued"),
            @ApiResponse(responseCode = "401", description = "Refresh token invalid or expired"),
            @ApiResponse(responseCode = "429", description = "Too many attempts — rate limited")
    })
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

    @Operation(
            summary = "Logout",
            description = "Invalidates the refresh token. " +
                    "The access token will expire naturally after 15 minutes. " +
                    "After logout, the refresh token cannot be used to generate new access tokens."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Logged out successfully"),
            @ApiResponse(responseCode = "400", description = "Refresh token is required")
    })
    @PostMapping("/logout")
    public ResponseEntity<String> logout(
            @Valid @RequestBody RefreshTokenRequest request) {
        refreshTokenService.deleteByToken(request.getRefreshToken());
        return ResponseEntity.ok("Logged out successfully");
    }
}