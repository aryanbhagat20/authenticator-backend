package com.aryanhagat.authenticator.service;

// Spring imports
import org.springframework.stereotype.Service;

// Java imports
import java.security.SecureRandom;


// ZXing imports for QR code generation
import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;

// OTP generation imports
import com.eatthepath.otp.TimeBasedOneTimePasswordGenerator;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.time.Instant;

// I/O imports
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import org.apache.commons.codec.binary.Base32;


@Service
public class TwoFactorService {

    private static final SecureRandom secureRandom = new SecureRandom();

    public String generateSecret() {
        byte[] bytes = new byte[20]; // 160 bits
        new SecureRandom().nextBytes(bytes);

        Base32 base32 = new Base32();
        return base32.encodeToString(bytes).replace("=", "");
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

    public boolean verifyOtp(String base32Secret, int otp) {

        try {
            Base32 base32 = new Base32();
            byte[] decodedKey = base32.decode(base32Secret);

            SecretKey secretKey = new SecretKeySpec(decodedKey, "HmacSHA1");

            TimeBasedOneTimePasswordGenerator totp =
                    new TimeBasedOneTimePasswordGenerator();

            Instant now = Instant.now();

            // Allow clock skew: previous, current, next window
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
