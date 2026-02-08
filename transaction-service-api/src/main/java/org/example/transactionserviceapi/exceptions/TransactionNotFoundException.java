package org.example.transactionserviceapi.exceptions;

public class TransactionNotFoundException extends RuntimeException{
    public TransactionNotFoundException(String message){
        super(message);
    }
}
