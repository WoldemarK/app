package org.example.transactionserviceapi.service;

import org.example.transactionapp.dto.CreateWalletRequest;
import org.example.transactionserviceapi.entity.Wallet;
import org.example.transactionserviceapi.entity.WalletType;
import org.example.transactionserviceapi.entity.WalletTypeStatus;
import org.example.transactionserviceapi.exceptions.WalletNotFoundException;
import org.example.transactionserviceapi.metrics.MetricsFacade;
import org.example.transactionserviceapi.repository.WalletRepository;
import org.example.transactionserviceapi.repository.WalletTypeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class WalletServiceImplTest {

    @Mock
    private WalletRepository walletRepository;

    @Mock
    private WalletTypeRepository walletTypeRepository;

    @Mock
    private MetricsFacade metrics;

    @InjectMocks
    private WalletServiceImpl walletService;
    @Mock
    private Wallet wallet;
    @Mock
    private WalletType walletType;
    @Mock
    private CreateWalletRequest createWalletRequest;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        walletType = WalletType.builder()
                .currencyCode("USD")
                .status(WalletTypeStatus.ACTIVE)
                .build();

        createWalletRequest = new CreateWalletRequest();
        createWalletRequest.setCurrency("USD");
        createWalletRequest.setUserUid(UUID.randomUUID());
        createWalletRequest.setInitialBalance(100.00);

        wallet = new Wallet();
        wallet.setId(UUID.randomUUID());
        wallet.setUserUid(createWalletRequest.getUserUid());
        wallet.setName("USD wallet");
        wallet.setWalletType(walletType);
        wallet.setStatus(WalletTypeStatus.ACTIVE);
        wallet.setBalance(BigDecimal.valueOf(100.00));
    }

    @Test
    void testCreateWallet_Success() {
        // Given
        when(walletTypeRepository.findByCurrencyCodeAndStatus("USD", WalletTypeStatus.ACTIVE))
                .thenReturn(Optional.of(walletType));
        when(walletRepository.existsByUserUidAndWalletType(createWalletRequest.getUserUid(), walletType))
                .thenReturn(false);
        when(walletRepository.save(any(Wallet.class))).thenReturn(wallet);

        // When
        Wallet createdWallet = walletService.createWallet(createWalletRequest);

        // Then
        assertNotNull(createdWallet);
        assertEquals("USD wallet", createdWallet.getName());
        assertEquals(createWalletRequest.getUserUid(), createdWallet.getUserUid());
        assertEquals(BigDecimal.valueOf(100.00), createdWallet.getBalance());

        verify(walletRepository, times(1)).save(any(Wallet.class));
        verify(metrics, times(1)).walletCreated("USD");
    }

    @Test
    void testCreateWallet_WalletTypeNotFound() {
        // Given
        when(walletTypeRepository.findByCurrencyCodeAndStatus("USD", WalletTypeStatus.ACTIVE))
                .thenReturn(Optional.empty());

        // When & Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> walletService.createWallet(createWalletRequest));
        assertEquals("Active WalletType not found for currency: USD", exception.getMessage());

        verify(metrics, times(1)).walletCreateError("wallet_type_not_found");
    }

    @Test
    void testCreateWallet_AlreadyExists() {
        // Given
        when(walletTypeRepository.findByCurrencyCodeAndStatus("USD", WalletTypeStatus.ACTIVE))
                .thenReturn(Optional.of(walletType));
        when(walletRepository.existsByUserUidAndWalletType(createWalletRequest.getUserUid(), walletType))
                .thenReturn(true);

        // When & Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            walletService.createWallet(createWalletRequest);
        });
        assertEquals("Wallet already exists for currency USD", exception.getMessage());

        verify(metrics, times(1)).walletCreateError("already_exists");
    }

    @Test
    void testGetInformationByWalletId_Success() {
        // Given
        UUID walletId = wallet.getId();
        when(walletRepository.findById(walletId)).thenReturn(Optional.of(wallet));

        // When
        Wallet fetchedWallet = walletService.getInformationByWalletId(walletId);

        // Then
        assertNotNull(fetchedWallet);
        assertEquals(walletId, fetchedWallet.getId());

        verify(metrics, times(1)).walletFetched(true);
    }

    @Test
    void testGetInformationByWalletId_WalletNotFound() {
        // Given
        UUID walletId = UUID.randomUUID();
        when(walletRepository.findById(walletId)).thenReturn(Optional.empty());

        // When & Then
        WalletNotFoundException exception = assertThrows(WalletNotFoundException.class,
                () -> walletService.getInformationByWalletId(walletId));
        assertEquals("Активный кошелек не найден: " + walletId, exception.getMessage());

        verify(metrics, times(1)).walletFetched(false);
    }
}