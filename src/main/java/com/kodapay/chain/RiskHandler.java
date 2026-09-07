package com.kodapay.chain;

import com.kodapay.domain.Payment;

/**
 * Padrão <b>Chain of Responsibility</b> (GoF comportamental): cada handler
 * avalia uma regra de risco sobre a transação, registra o resultado na trilha
 * de auditoria e repassa para o próximo elo com {@code chain.proceed(...)}.
 *
 * <p>Para vetar a transação, o handler lança
 * {@link com.kodapay.exception.RiskViolationException} — a requisição não
 * segue para os elos seguintes.</p>
 *
 * <p>A ordem dos elos é definida em {@code RiskChainConfig}, não aqui:
 * regra nova entra sem alterar as existentes.</p>
 */
public interface RiskHandler {

    /** Nome exibido na trilha de auditoria. */
    String name();

    /**
     * Avalia a regra. Fluxo esperado: registrar auditoria com
     * {@code chain.record(...)} e chamar {@code chain.proceed(payment)}
     * para liberar a transação — ou vetar lançando a exceção.
     */
    void handle(Payment payment, RiskChain chain);
}
