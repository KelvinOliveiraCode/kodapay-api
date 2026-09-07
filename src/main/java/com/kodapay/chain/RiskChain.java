package com.kodapay.chain;

import com.kodapay.domain.AuditEvent;
import com.kodapay.domain.Payment;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Estrutura da cadeia: mantém os elos na ordem configurada, acumula a trilha
 * de auditoria e avança um elo por vez conforme cada handler aprova.
 */
public class RiskChain {

    private final List<RiskHandler> handlers;
    private final List<AuditEvent> trail = new ArrayList<>();
    private int index = 0;

    public RiskChain(List<RiskHandler> handlers) {
        this.handlers = List.copyOf(handlers);
    }

    /** Processa a transação do primeiro ao último elo; devolve a auditoria completa. */
    public List<AuditEvent> run(Payment payment) {
        proceed(payment);
        return Collections.unmodifiableList(trail);
    }

    /** Registra um evento de auditoria na trilha, na ordem em que ocorre. */
    public void record(AuditEvent event) {
        trail.add(event);
    }

    /** Passa a transação para o próximo elo (ou encerra se chegou ao fim). */
    public void proceed(Payment payment) {
        if (index < handlers.size()) {
            RiskHandler handler = handlers.get(index++);
            handler.handle(payment, this);
        }
    }
}
