package com.kodapay.domain;

/** Ciclo de vida de uma transação (simulado — nenhum gateway externo é chamado). */
public enum PaymentStatus {
    APPROVED, FAILED, REJECTED
}
