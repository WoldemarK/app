package org.example.transactionserviceapi.repository;

import jakarta.persistence.LockModeType;

import org.example.transactionserviceapi.entity.Wallet;
import org.example.transactionserviceapi.entity.WalletType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface WalletRepository extends JpaRepository<Wallet, UUID> {

    boolean existsByUserUidAndWalletType(UUID userUid, WalletType walletType);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select w from Wallet w where w.id = :id")
    Optional<Wallet> findByIdForUpdate(@Param("id") UUID id);


    Optional<Wallet> findByUserUidAndId(UUID userUid, UUID id);
}
