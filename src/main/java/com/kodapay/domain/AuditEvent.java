package com.kodapay.domain;

import java.time.OffsetDateTime;
import java.util.UUID;

/** Registro de auditoria anexado a uma transação pelo Chain of Responsibility. */
public record AuditEvent(
        OffsetDateTime timestamp,
        UUID paymentId,
        String source,
        String message
) {
}
