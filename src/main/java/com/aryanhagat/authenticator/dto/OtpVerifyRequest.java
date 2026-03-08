package com.aryanhagat.authenticator.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

@Schema(description = "Request body for enabling 2FA")
public class OtpVerifyRequest {

    @Schema(description = "Email (optional — server uses JWT identity)", example = "user@gmail.com")
    private String email;

    @Schema(description = "6-digit OTP from authenticator app", example = "123456")
    @NotBlank(message = "OTP is required")
    @Pattern(regexp = "^[0-9]{6}$", message = "OTP must be exactly 6 digits")
    private String otp;

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getOtp() { return otp; }
    public void setOtp(String otp) { this.otp = otp; }
}