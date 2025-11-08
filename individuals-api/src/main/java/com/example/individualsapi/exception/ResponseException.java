package com.example.individualsapi.exception;

public class ResponseException extends RuntimeException{
    public ResponseException(String message){
        super(message);
    }
}
