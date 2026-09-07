package com.kodapay.chain;

import com.kodapay.domain.AuditEvent;
import com.kodapay.domain.Payment;
import com.kodapay.exception.RiskViolationException;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.time.OffsetDateTime;

/**
 * Elo 3 — velocidade: mais de 3 transações do mesmo pagador na última hora
 * caracteriza comportamento anômalo e veto.
 */
@Component
@Order(3)
public class VelocityHandler implements RiskHandler {

    private final PaymentRegistry registry;

    public VelocityHandler(PaymentRegistry registry) {
        this.registry = registry;
    }

    @Override
    public String name() {
        return "VELOCITY";
    }

    @Override
    public void handle(Payment payment, RiskChain chain) {
        long ultimas1h = registry.countByPayerSince(
                payment.payerName(),
                OffsetDateTime.now().minusHours(1));
        if (ultimas1h >= 3) {
            chain.record(new AuditEvent(OffsetDateTime.now(), payment.id(), name(),
                    "vetado: " + ultimas1h + " transações na última hora"));
            throw new RiskViolationException(
                    "Pagador com " + ultimas1h
                            + " transações na última hora: limite de velocidade excedido.");
        }
        chain.record(new AuditEvent(OffsetDateTime.now(), payment.id(), name(),
                "aprovado: " + ultimas1h + " transação(ões) na última hora"));
        chain.proceed(payment);
    }
}
