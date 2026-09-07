package com.kodapay.strategy;

import com.kodapay.domain.PaymentMethod;

import java.math.BigDecimal;

/**
 * Cartão de crédito: percentual de 3,99% sobre o valor.
 *
 * <p>Implementa {@link FeeCalculator} para o método {@code CREDIT_CARD}.</p>
 */
@org.springframework.stereotype.Component
public class CreditCardFeeCalculator implements FeeCalculator {

    private static final BigDecimal RATE = new BigDecimal("0.0399");

    @Override
    public PaymentMethod supports() {
        return PaymentMethod.CREDIT_CARD;
    }

    @Override
    public BigDecimal calculateFee(BigDecimal baseAmount) {
        return baseAmount.multiply(RATE).setScale(2, java.math.RoundingMode.HALF_EVEN);
    }
}
