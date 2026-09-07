package com.kodapay.strategy;

import com.kodapay.domain.PaymentMethod;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Testes unitários das estratégias de taxa — cada método de pagamento tem a
 * sua política e as contas exatas são verificadas.
 */
class FeeCalculatorTest {

    private final CreditCardFeeCalculator credit = new CreditCardFeeCalculator();
    private final DebitCardFeeCalculator debit = new DebitCardFeeCalculator();
    private final PixFeeCalculator pix = new PixFeeCalculator();
    private final BoletoFeeCalculator boleto = new BoletoFeeCalculator();

    @Test
    @DisplayName("Cartão de crédito cobra 3,99%: R$100 vira taxa de R$3,99")
    void creditCardPercentage() {
        assertThat(credit.calculateFee(new BigDecimal("100.00")))
                .isEqualByComparingTo("3.99");
    }

    @Test
    @DisplayName("Crédito arredonda para 2 casas: R$33,33 vira taxa de R$1,33")
    void creditCardRoundsToTwoDecimals() {
        assertThat(credit.calculateFee(new BigDecimal("33.33")))
                .isEqualByComparingTo("1.33");
    }

    @Test
    @DisplayName("Cartão de débito cobra 1,89%: R$200 vira taxa de R$3,78")
    void debitCardPercentage() {
        assertThat(debit.calculateFee(new BigDecimal("200.00")))
                .isEqualByComparingTo("3.78");
    }

    @Test
    @DisplayName("Débito arredonda para 2 casas: R$99,90 vira taxa de R$1,89")
    void debitCardRoundsToTwoDecimals() {
        assertThat(debit.calculateFee(new BigDecimal("99.90")))
                .isEqualByComparingTo("1.89");
    }

    @Test
    @DisplayName("Pix tem taxa fixa de R$0,49")
    void pixFixedFee() {
        assertThat(pix.calculateFee(new BigDecimal("37.50")))
                .isEqualByComparingTo("0.49");
    }

    @Test
    @DisplayName("Boleto tem taxa fixa de R$2,49")
    void boletoFixedFee() {
        assertThat(boleto.calculateFee(new BigDecimal("37.50")))
                .isEqualByComparingTo("2.49");
    }

    @Test
    @DisplayName("Cada estratégia declara o método que atende")
    void supportsCorrectMethod() {
        assertThat(credit.supports()).isEqualTo(PaymentMethod.CREDIT_CARD);
        assertThat(debit.supports()).isEqualTo(PaymentMethod.DEBIT_CARD);
        assertThat(pix.supports()).isEqualTo(PaymentMethod.PIX);
        assertThat(boleto.supports()).isEqualTo(PaymentMethod.BOLETO);
    }
}
