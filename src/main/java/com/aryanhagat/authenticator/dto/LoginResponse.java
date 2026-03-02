package com.aryanhagat.authenticator.dto;

public class LoginResponse {

    private boolean success;
    private boolean otpRequired;
    private String message;
    private String token;

    // Constructor for when we have a token (successful login, 2FA not required)
    public LoginResponse(boolean success, boolean otpRequired, String message, String token) {
        this.success = success;
        this.otpRequired = otpRequired;
        this.message = message;
        this.token = token;
    }

    // Constructor for when we don't have a token yet (2FA required)
    public LoginResponse(boolean success, boolean otpRequired, String message) {
        this.success = success;
        this.otpRequired = otpRequired;
        this.message = message;
        this.token = null;
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

    public String getToken() {
        return token;
    }
}