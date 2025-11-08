package com.example.individualsapi.exception;

public class ValidationError extends ResponseException {
    public ValidationError(String message) {
        super(message);
    }
}
