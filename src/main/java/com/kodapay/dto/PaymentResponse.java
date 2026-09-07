package com.kodapay.dto;

import com.kodapay.domain.AuditEvent;
import com.kodapay.domain.Payment;

import java.util.List;

/**
 * Resposta da criação de um pagamento: a transação processada e a trilha de
 * auditoria gerada pelo Chain of Responsibility.
 */
public record PaymentResponse(
        Payment payment,
        List<String> auditTrail
) {
    public static PaymentResponse from(Payment p, List<AuditEvent> audit) {
        return new PaymentResponse(
                p,
                audit.stream()
                        .map(e -> "[%s] %s".formatted(e.source(), e.message()))
                        .toList());
    }
}
