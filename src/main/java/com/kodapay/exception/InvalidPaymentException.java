package com.kodapay.exception;

/** 400 — payload inválido (violação de Bean Validation ou regra de negócio). */
public class InvalidPaymentException extends RuntimeException {
    public InvalidPaymentException(String message) {
        super(message);
    }
}
