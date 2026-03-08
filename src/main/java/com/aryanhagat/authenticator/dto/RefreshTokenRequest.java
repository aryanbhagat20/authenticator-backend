package com.aryanhagat.authenticator.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "Request body for refreshing access token or logging out")
public class RefreshTokenRequest {

    @Schema(
            description = "The refresh token received during login",
            example = "550e8400-e29b-41d4-a716-446655440000"
    )
    @NotBlank(message = "Refresh token is required")
    private String refreshToken;

    public String getRefreshToken() { return refreshToken; }
    public void setRefreshToken(String refreshToken) { this.refreshToken = refreshToken; }
}