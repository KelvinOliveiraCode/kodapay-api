package com.kodapay.domain;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * Entidade imutável que representa uma transação processada pela KodaPay.
 * Os campos calculados (taxa e total) são definidos uma única vez no processamento
 * e não mudam mais: o registro é um histórico do que aconteceu.
 */
public record Payment(
        UUID id,
        String payerName,
        String description,
        BigDecimal amount,
        PaymentMethod method,
        BigDecimal fee,
        BigDecimal total,
        PaymentStatus status,
        OffsetDateTime createdAt
) {
}
