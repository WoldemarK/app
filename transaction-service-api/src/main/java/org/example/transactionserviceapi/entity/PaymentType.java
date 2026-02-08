package org.example.transactionserviceapi.entity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

@Getter
@ToString
@RequiredArgsConstructor
public enum PaymentType {
    DEPOSIT("deposit"),
    WITHDRAWAL("withdrawal"),
    TRANSFER("transfer");

    private final String value;


}
