package com.aryanhagat.authenticator.dto;

public class LoginResponse {

    private boolean success;
    private boolean otpRequired;
    private String message;
    private String token;
    private String refreshToken; // NEW

    // Full constructor — login success with both tokens
    public LoginResponse(boolean success, boolean otpRequired,
                         String message, String token, String refreshToken) {
        this.success = success;
        this.otpRequired = otpRequired;
        this.message = message;
        this.token = token;
        this.refreshToken = refreshToken;
    }

    // No token constructor — used when 2FA is required
    public LoginResponse(boolean success, boolean otpRequired, String message) {
        this.success = success;
        this.otpRequired = otpRequired;
        this.message = message;
        this.token = null;
        this.refreshToken = null;
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

    public String getRefreshToken() {
        return refreshToken;
    }
}