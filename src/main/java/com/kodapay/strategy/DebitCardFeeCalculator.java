package com.kodapay.strategy;

import com.kodapay.domain.PaymentMethod;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Cartão de débito: percentual de 1,89% sobre o valor.
 *
 * <p>Implementa {@link FeeCalculator} para o método {@code DEBIT_CARD}.</p>
 */
@org.springframework.stereotype.Component
public class DebitCardFeeCalculator implements FeeCalculator {

    private static final BigDecimal RATE = new BigDecimal("0.0189");

    @Override
    public PaymentMethod supports() {
        return PaymentMethod.DEBIT_CARD;
    }

    @Override
    public BigDecimal calculateFee(BigDecimal baseAmount) {
        return baseAmount.multiply(RATE).setScale(2, RoundingMode.HALF_EVEN);
    }
}
