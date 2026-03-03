package com.aryanhagat.authenticator.service;

import com.aryanhagat.authenticator.entity.User;
import com.aryanhagat.authenticator.exception.InvalidCredentialsException;
import com.aryanhagat.authenticator.exception.UserNotFoundException;
import com.aryanhagat.authenticator.repository.UserRepository;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import org.apache.commons.codec.binary.Base32;
import org.springframework.stereotype.Service;

import com.eatthepath.otp.TimeBasedOneTimePasswordGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.security.SecureRandom;
import java.time.Instant;

@Service
public class TwoFactorService {
    private static final SecureRandom secureRandom = new SecureRandom();

    private final UserRepository userRepository;

    public TwoFactorService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }


    // BUSINESS METHOD: Get QR code for the current user
    // Called by controller with email from JWT token
    public byte[] getQrCodeForUser(String email) {

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        String otpAuthUri = buildOtpAuthUri(
                user.getEmail(),
                user.getTwoFactorSecret()
        );

        return generateQrCode(otpAuthUri);
    }

    // BUSINESS METHOD: Enable 2FA for the current user
    // Called by controller with email from JWT token
    public void enableTwoFactor(String email, Integer otp) {

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        boolean isValid = verifyOtp(user.getTwoFactorSecret(), otp);

        if (!isValid) {
            throw new InvalidCredentialsException("Invalid OTP");
        }

        user.setTwoFactorEnabled(true);
        userRepository.save(user);
    }


    // UTILITY: Generate a new TOTP secret
    public String generateSecret() {
        byte[] bytes = new byte[20];
        new SecureRandom().nextBytes(bytes);
        Base32 base32 = new Base32();
        return base32.encodeToString(bytes).replace("=", "");
    }

    // UTILITY: Build the otpauth:// URI for QR code
    public String buildOtpAuthUri(String email, String secret) {
        String issuer = "AuthenticatorApp";
        return String.format(
                "otpauth://totp/%s:%s?secret=%s&issuer=%s",
                issuer,
                email,
                secret,
                issuer
        );
    }


    // UTILITY: Generate QR code as PNG bytes
    public byte[] generateQrCode(String otpAuthUri) {
        try {
            QRCodeWriter qrCodeWriter = new QRCodeWriter();
            BitMatrix bitMatrix = qrCodeWriter.encode(
                    otpAuthUri,
                    BarcodeFormat.QR_CODE,
                    300,
                    300
            );
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            MatrixToImageWriter.writeToStream(bitMatrix, "PNG", outputStream);
            return outputStream.toByteArray();
        } catch (WriterException | IOException e) {
            throw new RuntimeException("Failed to generate QR code", e);
        }
    }


    // UTILITY: Verify a TOTP code against a secret
    public boolean verifyOtp(String base32Secret, Integer otp) {
        try {
            Base32 base32 = new Base32();
            byte[] decodedKey = base32.decode(base32Secret);
            SecretKey secretKey = new SecretKeySpec(decodedKey, "HmacSHA1");

            TimeBasedOneTimePasswordGenerator totp =
                    new TimeBasedOneTimePasswordGenerator();

            Instant now = Instant.now();

            for (int i = -1; i <= 1; i++) {
                Instant time = now.plusSeconds(i * totp.getTimeStep().getSeconds());
                int generatedOtp = totp.generateOneTimePassword(secretKey, time);
                if (generatedOtp == otp) {
                    return true;
                }
            }
            return false;
        } catch (Exception e) {
            return false;
        }
    }
}