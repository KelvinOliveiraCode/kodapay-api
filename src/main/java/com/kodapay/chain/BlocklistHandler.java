package com.kodapay.chain;

import com.kodapay.domain.AuditEvent;
import com.kodapay.domain.Payment;
import com.kodapay.exception.RiskViolationException;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.time.OffsetDateTime;
import java.util.Locale;
import java.util.Set;

/**
 * Elo 2 — antifraude: pagador cujo nome consta na lista restrita é vetado.
 */
@Component
@Order(2)
public class BlocklistHandler implements RiskHandler {

    private static final Set<String> BLOCKLIST = Set.of(
            "FRAUDADOR DA SILVA", "TESTA DE FERRO SOUZA", "CARTEIRA PERDIDA LIMA");

    @Override
    public String name() {
        return "BLOCKLIST";
    }

    @Override
    public void handle(Payment payment, RiskChain chain) {
        String normalizado = payment.payerName()
                .trim()
                .toUpperCase(Locale.ROOT);
        if (BLOCKLIST.contains(normalizado)) {
            chain.record(new AuditEvent(OffsetDateTime.now(), payment.id(), name(),
                    "vetado: pagador consta na lista restrita"));
            throw new RiskViolationException("Pagador bloqueado pela lista restrita de risco.");
        }
        chain.record(new AuditEvent(OffsetDateTime.now(), payment.id(), name(),
                "aprovado: pagador não consta na lista restrita"));
        chain.proceed(payment);
    }
}
