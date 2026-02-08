package org.example.transactionserviceapi.rest;

import lombok.RequiredArgsConstructor;
import org.apache.juli.logging.Log;
import org.apache.juli.logging.LogFactory;
import org.example.transactionapp.dto.TransactionConfirmRequest;
import org.example.transactionapp.dto.TransactionConfirmResponse;
import org.example.transactionapp.dto.TransactionInitResponse;
import org.example.transactionapp.dto.TransactionStatusResponse;
import org.example.transactionserviceapi.service.HasAmount;
import org.example.transactionserviceapi.service.TransactionService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping(name = "/api/transactions/")
public class TransactionRestController {

    private static final Log LOG = LogFactory.getLog(TransactionRestController.class);
    private final TransactionService transactionService;

    // Инициализация транзакции
    @PostMapping("/transactions/{type}/init")
    public ResponseEntity<TransactionInitResponse> initTransaction(@PathVariable String type,
                                                                   @RequestBody HasAmount request) {
        LOG.info("{} : {} %s : %s".formatted(type, request.getAmount()));
        return ResponseEntity.ok(transactionService.init(type, request));
    }

    // Подтверждение транзакции
    @PostMapping("/transactions/{type}/confirm")
    public ResponseEntity<TransactionConfirmResponse> confirmTransaction(@PathVariable String type,
                                                                         @RequestBody TransactionConfirmRequest request) {
        LOG.info("{} : {} %s: %s".formatted(type, request));
        return ResponseEntity.ok(transactionService.confirm(type, request));
    }

    // Получение статуса транзакции
    @GetMapping("/transactions/{transactionId}/status")
    public ResponseEntity<TransactionStatusResponse> getTransactionStatus(@PathVariable UUID transactionId) {
        LOG.info("{} : %s ".formatted(transactionId));
        return ResponseEntity.ok(transactionService.status(transactionId));
    }

}
