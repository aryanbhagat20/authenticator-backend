package com.aryanhagat.authenticator.exception;

public class InvalidSignupException extends RuntimeException {
    public InvalidSignupException(String message) {
        super(message);
    }
}
