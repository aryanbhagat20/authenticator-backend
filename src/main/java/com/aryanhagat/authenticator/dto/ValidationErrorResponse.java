package com.aryanhagat.authenticator.dto;

import java.util.Map;

public class ValidationErrorResponse {

    private String message;
    private Map<String, String> errors;
    // Map of fieldName → errorMessage
    // Example: { "email": "Email is required", "password": "Password must be at least 6 characters" }

    public ValidationErrorResponse(String message, Map<String, String> errors) {
        this.message = message;
        this.errors = errors;
    }

    public String getMessage() {
        return message;
    }

    public Map<String, String> getErrors() {
        return errors;
    }
}