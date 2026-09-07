package com.kodapay.observer;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

/**
 * Editor (Subject) do Observer: entrega o evento a todos os listeners
 * registrados. Aqui o ApplicationEventPublisher do Spring faz o papel de
 * "lista de observadores + notifyAll".
 */
@Component
public class PaymentEventPublisher {

    private final ApplicationEventPublisher publisher;

    public PaymentEventPublisher(ApplicationEventPublisher publisher) {
        this.publisher = publisher;
    }

    /** Publica um evento do ciclo de vida de um pagamento. */
    public void publish(PaymentEvent event) {
        publisher.publishEvent(event);
    }
}
