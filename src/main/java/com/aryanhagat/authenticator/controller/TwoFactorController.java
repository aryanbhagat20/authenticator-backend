package com.aryanhagat.authenticator.controller;

import com.aryanhagat.authenticator.entity.User;
import com.aryanhagat.authenticator.repository.UserRepository;
import com.aryanhagat.authenticator.service.TwoFactorService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.aryanhagat.authenticator.dto.OtpVerifyRequest;



@RestController
@RequestMapping("/2fa")
public class TwoFactorController {

    private final UserRepository userRepository;
    private final TwoFactorService twoFactorService;

    public TwoFactorController(UserRepository userRepository,
                               TwoFactorService twoFactorService) {
        this.userRepository = userRepository;
        this.twoFactorService = twoFactorService;
    }

    @GetMapping("/qr")
    public ResponseEntity<byte[]> getQrCode(@RequestParam String email) {

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        String otpAuthUri = twoFactorService.buildOtpAuthUri(
                user.getEmail(),
                user.getTwoFactorSecret()
        );

        byte[] qrImage = twoFactorService.generateQrCode(otpAuthUri);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_TYPE, MediaType.IMAGE_PNG_VALUE)
                .body(qrImage);
    }

    @PostMapping("/verify")
    public ResponseEntity<String> verifyOtp(@RequestBody OtpVerifyRequest request) {

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("User not found"));

        boolean isValid = twoFactorService.verifyOtp(
                user.getTwoFactorSecret(),
                request.getOtp()
        );

        if (!isValid) {
            return ResponseEntity
                    .badRequest()
                    .body("Invalid OTP");
        }

        user.setTwoFactorEnabled(true);
        userRepository.save(user);

        return ResponseEntity.ok("2FA enabled successfully");
    }


}
