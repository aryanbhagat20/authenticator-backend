package com.aryanhagat.authenticator.dto;

public class OtpVerifyRequest {

    private String email;
    private int otp;

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public int getOtp() {
        return otp;
    }

    public void setOtp(int otp) {
        this.otp = otp;
    }
}
