package com.kodapay.observer;

import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Observador 1 — mensageria: acumula notificações que "enviaria" por e-mail/SMS
 * (simulado em memória para o desafio).
 */
@Component
public class MessagingListener {

    private final List<String> sent = new ArrayList<>();

    @EventListener
    public void on(PaymentEvent event) {
        sent.add("[MSG] " + event.message() + " (pagamento " + event.payment().id() + ")");
    }

    /** Notificações acumuladas nesta instância. */
    public List<String> sentNotifications() {
        return Collections.unmodifiableList(sent);
    }
}
