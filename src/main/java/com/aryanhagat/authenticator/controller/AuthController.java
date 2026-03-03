package com.aryanhagat.authenticator.controller;

import com.aryanhagat.authenticator.dto.LoginOtpRequest;
import com.aryanhagat.authenticator.dto.SignupRequest;
import com.aryanhagat.authenticator.dto.LoginRequest;
import com.aryanhagat.authenticator.service.AuthService;
import com.aryanhagat.authenticator.dto.LoginResponse;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.Authentication;


@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/signup")
    public ResponseEntity<String> signup(@Valid @RequestBody SignupRequest request) {
        // @Valid triggers validation on SignupRequest before this method body runs
        authService.signup(request);
        return ResponseEntity.ok("User registered successfully");
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(
            @Valid @RequestBody LoginRequest request) {
        // @Valid triggers validation on LoginRequest
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

    // This endpoint is just for testing that authentication works — it returns the email of the logged-in user
    @GetMapping("/me")
    public ResponseEntity<String> getCurrentUser(Authentication authentication) {
        String email = authentication.getName();
        return ResponseEntity.ok("Logged in as: " + email);
    }
}