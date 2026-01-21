package com.aryanhagat.authenticator.dto;

public class LoginResponse {

    private boolean success;
    private boolean otpRequired;
    private String message;

    public LoginResponse(boolean success, boolean otpRequired, String message) {
        this.success = success;
        this.otpRequired = otpRequired;
        this.message = message;
    }

    public boolean isSuccess() {
        return success;
    }

    public boolean isOtpRequired() {
        return otpRequired;
    }

    public String getMessage() {
        return message;
    }
}
