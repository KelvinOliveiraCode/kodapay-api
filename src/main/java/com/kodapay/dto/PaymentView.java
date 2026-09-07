package com.kodapay.dto;

import com.kodapay.domain.Payment;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

/** Visão de uma transação devolvida ao cliente da API. */
public record PaymentView(
        UUID id,
        String payerName,
        String description,
        BigDecimal amount,
        String method,
        BigDecimal fee,
        BigDecimal total,
        String status,
        OffsetDateTime createdAt
) {
    public static PaymentView from(Payment p) {
        return new PaymentView(
                p.id(), p.payerName(), p.description(), p.amount(),
                p.method().name(), p.fee(), p.total(), p.status().name(), p.createdAt());
    }
}
