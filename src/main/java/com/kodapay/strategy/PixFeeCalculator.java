package com.kodapay.strategy;

import com.kodapay.domain.PaymentMethod;

import java.math.BigDecimal;

/**
 * Pix: taxa fixa de R$ 0,49 por transação, independentemente do valor.
 *
 * <p>Implementa {@link FeeCalculator} para o método {@code PIX}.</p>
 */
@org.springframework.stereotype.Component
public class PixFeeCalculator implements FeeCalculator {

    private static final BigDecimal FIXED_FEE = new BigDecimal("0.49");

    @Override
    public PaymentMethod supports() {
        return PaymentMethod.PIX;
    }

    @Override
    public BigDecimal calculateFee(BigDecimal baseAmount) {
        return FIXED_FEE;
    }
}
