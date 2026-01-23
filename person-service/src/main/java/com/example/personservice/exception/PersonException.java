package com.example.personservice.exception;

public class PersonException extends RuntimeException {

    public PersonException(String message, Object... args) {
        super(String.format(message, args));
    }
}
