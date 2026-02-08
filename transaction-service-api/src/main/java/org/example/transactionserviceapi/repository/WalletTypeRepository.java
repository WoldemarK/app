package org.example.transactionserviceapi.repository;

import org.example.transactionserviceapi.entity.WalletType;
import org.example.transactionserviceapi.entity.WalletTypeStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface WalletTypeRepository extends JpaRepository<WalletType, UUID> {
    Optional<WalletType> findByCurrencyCodeAndStatus(String currencyCode, WalletTypeStatus status);
}
