package org.example.transactionserviceapi.exceptions;

public class WalletNotFoundException extends RuntimeException{
    public WalletNotFoundException(String message){
        super(message);
    }
}
