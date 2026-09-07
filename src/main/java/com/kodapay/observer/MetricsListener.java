package com.kodapay.observer;

import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Observador 2 — métricas: registra contadores simples por método de
 * pagamento (simulado em memória para o desafio).
 */
@Component
public class MetricsListener {

    private final List<String> counters = new ArrayList<>();

    @EventListener
    public void on(PaymentEvent event) {
        counters.add("[METRICS] " + event.payment().method() + " -> " + event.message());
    }

    /** Contadores acumulados nesta instância. */
    public List<String> collectedMetrics() {
        return Collections.unmodifiableList(counters);
    }
}

