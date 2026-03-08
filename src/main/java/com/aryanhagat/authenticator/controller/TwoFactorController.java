package com.aryanhagat.authenticator.controller;

import com.aryanhagat.authenticator.dto.OtpVerifyRequest;
import com.aryanhagat.authenticator.service.TwoFactorService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/2fa")
@Tag(name = "Two-Factor Authentication", description = "Endpoints for setting up and verifying 2FA via TOTP")
public class TwoFactorController {

    private final TwoFactorService twoFactorService;

    public TwoFactorController(TwoFactorService twoFactorService) {
        this.twoFactorService = twoFactorService;
    }

    @Operation(
            summary = "Get 2FA QR code",
            description = "Returns a QR code image (PNG) for the authenticated user. " +
                    "Scan this with Google Authenticator or Authy to set up 2FA. " +
                    "Requires a valid JWT token — log in first to get one."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "QR code PNG image returned"),
            @ApiResponse(responseCode = "401", description = "Not authenticated — JWT required"),
            @ApiResponse(responseCode = "404", description = "User not found")
    })
    @SecurityRequirement(name = "Bearer Authentication")
    @GetMapping("/qr")
    public ResponseEntity<byte[]> getQrCode(Authentication authentication) {
        String email = authentication.getName();
        byte[] qrImage = twoFactorService.getQrCodeForUser(email);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_TYPE, MediaType.IMAGE_PNG_VALUE)
                .body(qrImage);
    }

    @Operation(
            summary = "Enable 2FA — verify OTP",
            description = "Verifies the OTP from your authenticator app and enables 2FA on your account. " +
                    "After calling this, every login will require both password and OTP. " +
                    "Requires a valid JWT token."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "2FA enabled successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid input or OTP format"),
            @ApiResponse(responseCode = "401", description = "Invalid OTP or not authenticated"),
            @ApiResponse(responseCode = "404", description = "User not found")
    })
    @SecurityRequirement(name = "Bearer Authentication")
    @PostMapping("/verify")
    public ResponseEntity<String> verifyOtp(
            @Valid @RequestBody OtpVerifyRequest request,
            Authentication authentication) {
        String email = authentication.getName();
        twoFactorService.enableTwoFactor(email, request.getOtp());
        return ResponseEntity.ok("2FA enabled successfully");
    }
}