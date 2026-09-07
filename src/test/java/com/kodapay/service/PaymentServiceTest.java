package com.kodapay.service;

import com.kodapay.chain.PaymentRegistry;
import com.kodapay.chain.RiskChainFactory;
import com.kodapay.domain.PaymentMethod;
import com.kodapay.dto.PaymentRequest;
import com.kodapay.exception.InvalidPaymentException;
import com.kodapay.exception.RiskViolationException;
import com.kodapay.observer.PaymentEvent;
import com.kodapay.observer.PaymentEventPublisher;
import com.kodapay.strategy.BoletoFeeCalculator;
import com.kodapay.strategy.CreditCardFeeCalculator;
import com.kodapay.strategy.DebitCardFeeCalculator;
import com.kodapay.strategy.FeeCalculator;
import com.kodapay.strategy.PixFeeCalculator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PaymentServiceTest {

    private PaymentRegistry registry;
    private PaymentEventPublisher publisher;
    private PaymentService service;

    @BeforeEach
    void setUp() {
        registry = new PaymentRegistry();
        publisher = Mockito.mock(PaymentEventPublisher.class);
        List<FeeCalculator> calculators = List.of(
                new CreditCardFeeCalculator(),
                new DebitCardFeeCalculator(),
                new PixFeeCalculator(),
                new BoletoFeeCalculator());
        RiskChainFactory chainFactory = new RiskChainFactory(List.of(
                new com.kodapay.chain.HighValueHandler(),
                new com.kodapay.chain.BlocklistHandler(),
                new com.kodapay.chain.VelocityHandler(registry)));
        service = new PaymentService(calculators, chainFactory, registry, publisher);
    }

    private PaymentRequest request(String payer, String amount, PaymentMethod method) {
        return new PaymentRequest(payer, "compra teste", new BigDecimal(amount), method);
    }

    @Test
    @DisplayName("Crédito de R$100 gera taxa R$3,99 e total R$103,99 via Facade")
    void creditPaymentCalculatesFee() {
        var response = service.process(request("Kelvin Oliveira", "100.00",
                PaymentMethod.CREDIT_CARD));

        assertThat(response.payment().fee()).isEqualByComparingTo("3.99");
        assertThat(response.payment().total()).isEqualByComparingTo("103.99");
        assertThat(response.payment().status().name()).isEqualTo("APPROVED");
    }

    @Test
    @DisplayName("Pix de R$37,50 gera taxa fixa R$0,49 e total R$37,99")
    void pixPaymentUsesFixedFee() {
        var response = service.process(request("Kelvin Oliveira", "37.50",
                PaymentMethod.PIX));

        assertThat(response.payment().fee()).isEqualByComparingTo("0.49");
        assertThat(response.payment().total()).isEqualByComparingTo("37.99");
    }

    @Test
    @DisplayName("Veto de risco interrompe o pipeline e não persiste nem publica evento")
    void riskViolationStopsEverything() {
        assertThatThrownBy(() -> service.process(request("Fraudador da Silva", "100.00",
                PaymentMethod.PIX)))
                .isInstanceOf(RiskViolationException.class);

        assertThat(registry.findAll()).isEmpty();
        Mockito.verify(publisher, Mockito.never()).publish(Mockito.any());
    }

    @Test
    @DisplayName("Valor inválido é rejeitado antes de qualquer cálculo")
    void invalidAmountRejected() {
        assertThatThrownBy(() -> service.process(request("Kelvin", "0.00",
                PaymentMethod.PIX)))
                .isInstanceOf(InvalidPaymentException.class);
    }

    @Test
    @DisplayName("Observer é notificado com o total aprovado")
    void observerIsNotified() {
        service.process(request("Kelvin Oliveira", "100.00", PaymentMethod.CREDIT_CARD));

        ArgumentCaptor<PaymentEvent> captor = ArgumentCaptor.forClass(PaymentEvent.class);
        Mockito.verify(publisher).publish(captor.capture());
        assertThat(captor.getValue().message()).contains("103.99");
        assertThat(captor.getValue().payment().method()).isEqualTo(PaymentMethod.CREDIT_CARD);
    }

    @Test
    @DisplayName("History lista transações persistidas")
    void historyListsPayments() {
        service.process(request("Kelvin Oliveira", "50.00", PaymentMethod.PIX));
        service.process(request("Maria Souza", "75.00", PaymentMethod.BOLETO));

        assertThat(service.history()).hasSize(2);
    }
}
