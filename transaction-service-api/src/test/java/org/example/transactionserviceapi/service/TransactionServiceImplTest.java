package org.example.transactionserviceapi.service;

import org.example.transactionapp.dto.*;
import org.example.transactionserviceapi.entity.Transaction;
import org.example.transactionserviceapi.entity.TransactionStatus;
import org.example.transactionserviceapi.entity.Wallet;
import org.example.transactionserviceapi.entity.WalletType;
import org.example.transactionserviceapi.repository.TransactionRepository;
import org.example.transactionserviceapi.repository.WalletRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.kafka.core.KafkaTemplate;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;


class TransactionServiceImplTest {

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private WalletRepository walletRepository;

    @Mock
    private KafkaTemplate<String, Object> kafkaTemplate;

    @InjectMocks
    private TransactionServiceImpl transactionService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void init_shouldCalculateFeeAndTotalAmount() {
        HasAmount request = () -> BigDecimal.valueOf(100);
        HasAmount fee = () -> BigDecimal.valueOf(1.0);
        HasAmount total = () -> BigDecimal.valueOf(101.00);

        TransactionInitResponse response = transactionService.init("deposit", request);

        assertEquals(BigDecimal.valueOf(100), response.getAmount());
        assertEquals(0, response.getFee().compareTo(fee.getAmount()));
        assertEquals(0, response.getTotalAmount().compareTo(total.getAmount()));

        assertNotNull(response.getTransactionId());

        System.out.println(response);
    }


    @Test
    void confirm_deposit_shouldSendKafkaEvent() {
        UUID walletId = UUID.randomUUID();
        UUID transactionId = UUID.randomUUID();

        Wallet wallet = walletWithBalance(1000);

        when(walletRepository.findByIdForUpdate(walletId)).thenReturn(Optional.of(wallet));

        TransactionConfirmRequest request = confirmRequest(transactionId, walletId);

        TransactionConfirmResponse response = transactionService.confirm("deposit", request);

        verify(transactionRepository).save(any(Transaction.class));
        verify(kafkaTemplate).send(eq("deposit.requested"), any(DepositRequestedEvent.class));

        assertEquals(transactionId, response.getTransactionId());
        assertEquals(TransactionStatus.PROCESSING.name(), response.getStatus());
    }


    @Test
    void confirm_withdrawal_shouldSendKafkaEvent() {
        TransactionConfirmRequest request = confirmRequest(UUID.randomUUID(), UUID.randomUUID());
        transactionService.confirm("withdrawal", request);

        verify(kafkaTemplate).send(eq("withdrawal.requested"), anyString(), any(Transaction.class));
    }

    @Test
    void confirm_transfer_success_shouldMoveMoney() {
        UUID fromId = UUID.randomUUID();
        UUID toId = UUID.randomUUID();

        Wallet from = walletWithBalance(500);
        Wallet to = walletWithBalance(100);

        when(walletRepository.findByIdForUpdate(fromId)).thenReturn(Optional.of(from));
        when(walletRepository.findByIdForUpdate(toId)).thenReturn(Optional.of(to));

        TransactionConfirmRequest request = confirmRequest(UUID.randomUUID(), fromId);
        request.setTargetWalletUid(toId);
        request.setAmount(BigDecimal.valueOf(100));
        request.setFee(BigDecimal.valueOf(10));

        TransactionConfirmResponse response = transactionService.confirm("transfer", request);

        assertEquals(BigDecimal.valueOf(390.0), from.getBalance());
        assertEquals(BigDecimal.valueOf(200.0), to.getBalance());
        assertEquals(TransactionStatus.COMPLETED.name(), response.getStatus());

        verify(walletRepository).save(from);
        verify(walletRepository).save(to);
    }

    @Test
    void confirm_transfer_insufficientFunds_shouldFail() {
        UUID fromId = UUID.randomUUID();
        UUID toId = UUID.randomUUID();

        Wallet from = walletWithBalance(50);
        Wallet to = walletWithBalance(100);

        when(walletRepository.findByIdForUpdate(fromId)).thenReturn(Optional.of(from));
        when(walletRepository.findByIdForUpdate(toId)).thenReturn(Optional.of(to));

        TransactionConfirmRequest request = confirmRequest(UUID.randomUUID(), fromId);
        request.setTargetWalletUid(toId);
        request.setAmount(BigDecimal.valueOf(100));
        request.setFee(BigDecimal.valueOf(10));

        TransactionConfirmResponse response = transactionService.confirm("transfer", request);

        assertEquals(TransactionStatus.FAILED.name(), response.getStatus());

        verify(walletRepository, never()).save(any());
    }

    @Test
    void status_shouldReturnTransactionInfo() {
        UUID txId = UUID.randomUUID();

        Transaction transaction = Transaction.builder()
                .id(txId)
                .status(TransactionStatus.COMPLETED)
                .amount(BigDecimal.valueOf(100))
                .createdAt(OffsetDateTime.now())
                .build();

        when(transactionRepository.findById(txId)).thenReturn(Optional.of(transaction));

        TransactionStatusResponse response = transactionService.status(txId);

        assertEquals(txId, response.getTransactionId());
        assertEquals(TransactionStatus.COMPLETED.name(), response.getStatus());
    }

    private Wallet walletWithBalance(double balance) {
        return Wallet.builder()
                .balance(BigDecimal.valueOf(balance))
                .walletType(WalletType.builder()
                        .name("USD")
                        .build())
                .build();
    }

    private TransactionConfirmRequest confirmRequest(UUID txId, UUID walletId) {
        TransactionConfirmRequest request = new TransactionConfirmRequest();
        request.setTransactionId(txId);
        request.setWalletUid(walletId);
        request.setUserUid(UUID.randomUUID());
        request.setAmount(BigDecimal.valueOf(100));
        request.setFee(BigDecimal.valueOf(10));
        return request;
    }
}