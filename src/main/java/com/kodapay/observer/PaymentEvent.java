package com.kodapay.observer;

import com.kodapay.domain.Payment;

/** Evento publicado quando o ciclo de um pagamento termina (sucesso ou veto). */
public record PaymentEvent(Payment payment, String message) {
}
