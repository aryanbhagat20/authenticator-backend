package com.aryanhagat.authenticator.controller;

import com.aryanhagat.authenticator.dto.LoginOtpRequest;
import com.aryanhagat.authenticator.dto.SignupRequest;
import com.aryanhagat.authenticator.dto.LoginRequest;

import com.aryanhagat.authenticator.service.AuthService;
import com.aryanhagat.authenticator.dto.LoginResponse;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/signup")
    public ResponseEntity<String> signup(@RequestBody SignupRequest request) {
        authService.signup(request);
        return ResponseEntity.ok("User registered successfully");
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(
            @RequestBody LoginRequest request) {

        return ResponseEntity.ok(authService.login(request));
    }

    @PostMapping("/login/2fa")
    public ResponseEntity<LoginResponse> verifyLoginOtp(
            @RequestBody LoginOtpRequest request) {

        return ResponseEntity.ok(
                authService.verifyLoginOtp(
                        request.getEmail(),
                        request.getOtp()
                )
        );
    }





}
