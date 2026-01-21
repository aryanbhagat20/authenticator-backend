package com.aryanhagat.authenticator.service;

// Spring imports
import org.springframework.stereotype.Service;

// Java imports
import java.security.SecureRandom;
import java.util.Base64;

// ZXing imports for QR code generation
import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;

import java.io.ByteArrayOutputStream;
import java.io.IOException;


@Service
public class TwoFactorService {

    private static final SecureRandom secureRandom = new SecureRandom();

    public String generateSecret() {
        byte[] bytes = new byte[20]; // 160 bits
        secureRandom.nextBytes(bytes);
        return Base64.getEncoder().encodeToString(bytes);
    }

    public String buildOtpAuthUri(String email, String secret) { // OTP Auth URI format
        String issuer = "AuthenticatorApp";
        return String.format(
                "otpauth://totp/%s:%s?secret=%s&issuer=%s",
                issuer,
                email,
                secret,
                issuer
        );
    }

    // Generate QR code as a byte array
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
}
