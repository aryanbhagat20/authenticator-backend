package com.aryanhagat.authenticator.controller;

import com.aryanhagat.authenticator.dto.SignupRequest;
import com.aryanhagat.authenticator.dto.LoginRequest;

import com.aryanhagat.authenticator.service.AuthService;

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
    public ResponseEntity<String> login(@RequestBody LoginRequest request) {

        boolean loginCompleted = authService.login(request);

        if (loginCompleted) {
            return ResponseEntity.ok("Login successful");
        } else {
            return ResponseEntity.ok("OTP verification required");
        }
    }



}
