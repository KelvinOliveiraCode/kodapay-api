package com.kodapay.strategy;

import com.kodapay.domain.PaymentMethod;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Padrão <b>Strategy</b> (GoF comportamental). Contrato único para todos os
 * algoritmos de cálculo de taxa: cada método de pagamento implementa a sua
 * política sem que o restante do código precise conhecer as diferenças.
 *
 * <p>Registrar novas formas de pagamento = criar uma nova implementação e
 * anotá-la com {@code @Component}; nenhum ponto existente muda (princípio
 * Aberto/Fechado).</p>
 */
public interface FeeCalculator {
    /** Método ao qual esta estratégia se aplica. */
    PaymentMethod supports();

    /** Taxa cobrada sobre {@code baseAmount}, com 2 casas decimais (half-even). */
    BigDecimal calculateFee(BigDecimal baseAmount);
}
