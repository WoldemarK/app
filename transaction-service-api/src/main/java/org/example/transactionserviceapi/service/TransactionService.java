package org.example.transactionserviceapi.service;

import org.example.transactionapp.dto.TransactionConfirmRequest;
import org.example.transactionapp.dto.TransactionConfirmResponse;
import org.example.transactionapp.dto.TransactionInitResponse;
import org.example.transactionapp.dto.TransactionStatusResponse;

import java.util.UUID;

public interface TransactionService {

    TransactionInitResponse init(String type, HasAmount request);

    TransactionConfirmResponse confirm(String type, TransactionConfirmRequest request);

    TransactionStatusResponse status(UUID transactionId);
}
