package com.kodapay.strategy;

import com.kodapay.domain.PaymentMethod;

import java.math.BigDecimal;

/**
 * Boleto: taxa fixa de R$ 2,49 por transação, independentemente do valor.
 *
 * <p>Implementa {@link FeeCalculator} para o método {@code BOLETO}.</p>
 */
@org.springframework.stereotype.Component
public class BoletoFeeCalculator implements FeeCalculator {

    private static final BigDecimal FIXED_FEE = new BigDecimal("2.49");

    @Override
    public PaymentMethod supports() {
        return PaymentMethod.BOLETO;
    }

    @Override
    public BigDecimal calculateFee(BigDecimal baseAmount) {
        return FIXED_FEE;
    }
}
