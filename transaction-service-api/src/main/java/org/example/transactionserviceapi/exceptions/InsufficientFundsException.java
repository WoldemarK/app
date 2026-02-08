package org.example.transactionserviceapi.exceptions;

public class InsufficientFundsException extends RuntimeException{
    public InsufficientFundsException(String message){
        super(message);
    }
}
