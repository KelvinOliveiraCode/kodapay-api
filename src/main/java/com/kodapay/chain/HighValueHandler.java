package com.kodapay.chain;

import com.kodapay.domain.AuditEvent;
import com.kodapay.domain.Payment;
import com.kodapay.exception.RiskViolationException;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

/**
 * Elo 1 — valor: transações acima de R$ 15.000,00 exigem revisão manual
 * (vetadas automaticamente pelo canal digital).
 */
@Component
@Order(1)
public class HighValueHandler implements RiskHandler {

    static final BigDecimal LIMIT = new BigDecimal("15000.00");

    @Override
    public String name() {
        return "HIGH_VALUE";
    }

    @Override
    public void handle(Payment payment, RiskChain chain) {
        if (payment.amount().compareTo(LIMIT) > 0) {
            chain.record(new AuditEvent(OffsetDateTime.now(), payment.id(), name(),
                    "vetado: acima de R$ " + LIMIT + " exige revisão manual"));
            throw new RiskViolationException(
                    "Transação de R$ " + payment.amount() + " acima do limite de R$ "
                            + LIMIT + " para o canal digital.");
        }
        chain.record(new AuditEvent(OffsetDateTime.now(), payment.id(), name(),
                "aprovado: valor dentro do limite"));
        chain.proceed(payment);
    }
}
