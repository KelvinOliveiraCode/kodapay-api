package com.kodapay.exception;

/** 422 — a transação foi barrada por uma regra de risco (Chain of Responsibility). */
public class RiskViolationException extends RuntimeException {
    public RiskViolationException(String message) {
        super(message);
    }
}
