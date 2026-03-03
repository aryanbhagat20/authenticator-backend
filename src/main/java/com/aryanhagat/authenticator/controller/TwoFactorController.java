package com.aryanhagat.authenticator.controller;

import com.aryanhagat.authenticator.dto.OtpVerifyRequest;
import com.aryanhagat.authenticator.service.TwoFactorService;
import jakarta.validation.Valid;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/2fa")
public class TwoFactorController {

    private final TwoFactorService twoFactorService;

    public TwoFactorController(TwoFactorService twoFactorService) {
        this.twoFactorService = twoFactorService;
    }

    @GetMapping("/qr")
    public ResponseEntity<byte[]> getQrCode(Authentication authentication) {

        // authentication.getName() returns the email we stored in the JWT subject
        String email = authentication.getName();

        byte[] qrImage = twoFactorService.getQrCodeForUser(email);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_TYPE, MediaType.IMAGE_PNG_VALUE)
                .body(qrImage);
    }

    @PostMapping("/verify")
    public ResponseEntity<String> verifyOtp(
            @Valid @RequestBody OtpVerifyRequest request,
            Authentication authentication) {

        // The email comes from the JWT token — the client cannot lie about who they are
        String email = authentication.getName();

        twoFactorService.enableTwoFactor(email, request.getOtp());

        return ResponseEntity.ok("2FA enabled successfully");
    }
}