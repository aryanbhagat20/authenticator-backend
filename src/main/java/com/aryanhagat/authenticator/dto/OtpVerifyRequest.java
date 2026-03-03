package com.aryanhagat.authenticator.dto;

import jakarta.validation.constraints.NotNull;

public class OtpVerifyRequest {

    private String email;

    @NotNull(message = "OTP is required")
    private Integer otp;

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Integer getOtp() {
        return otp;
    }

    public void setOtp(Integer otp) {
        this.otp = otp;
    }
}