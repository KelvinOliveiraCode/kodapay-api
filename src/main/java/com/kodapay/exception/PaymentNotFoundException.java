package com.kodapay.exception;

import java.util.UUID;

/** 404 — recurso inexistente. */
public class PaymentNotFoundException extends RuntimeException {
    public PaymentNotFoundException(UUID id) {
        super("Pagamento " + id + " nao encontrado.");
    }
}
