package org.example.transactionserviceapi.service;
import org.example.transactionapp.dto.CreateWalletRequest;
import org.example.transactionserviceapi.entity.Wallet;

import java.util.UUID;

public interface WalletService {

     Wallet createWallet(CreateWalletRequest request);

    Wallet getInformationByWalletId(UUID walletId);

}
