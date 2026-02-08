package org.example.transactionserviceapi.exceptions;

public class TransactionIsNotPresent extends RuntimeException{
    public TransactionIsNotPresent(String message) {
        super(message);
    }
}
