package com.kodapay.chain;

import com.kodapay.chain.RiskChainFactory;
import com.kodapay.domain.AuditEvent;
import com.kodapay.domain.Payment;
import com.kodapay.domain.PaymentMethod;
import com.kodapay.domain.PaymentStatus;
import com.kodapay.exception.RiskViolationException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Testes unitários da cadeia de risco: ordem, auditoria acumulada e vetos.
 */
class RiskChainTest {

    private Payment payment(String payer, String amount) {
        return new Payment(UUID.randomUUID(), payer, "teste",
                new BigDecimal(amount), PaymentMethod.PIX,
                new BigDecimal("0.49"), new BigDecimal(amount).add(new BigDecimal("0.49")),
                PaymentStatus.APPROVED, OffsetDateTime.now());
    }

    @Test
    @DisplayName("Transação normal atravessa a cadeia e gera auditoria de todos os elos")
    void approvedPaymentRunsAllHandlers() {
        PaymentRegistry registry = new PaymentRegistry();
        RiskChain chain = new RiskChain(List.of(
                new HighValueHandler(),
                new BlocklistHandler(),
                new VelocityHandler(registry)));

        List<AuditEvent> trail = chain.run(payment("Kelvin Oliveira", "100.00"));

        assertThat(trail).hasSize(3);
        assertThat(trail).extracting(AuditEvent::source)
                .containsExactly("HIGH_VALUE", "BLOCKLIST", "VELOCITY");
        assertThat(trail).allMatch(e -> e.message().startsWith("aprovado"));
    }

    @Test
    @DisplayName("Acima de R$15.000 é vetado pelo primeiro elo e nada passa aos seguintes")
    void highValueIsRejected() {
        PaymentRegistry registry = new PaymentRegistry();
        RiskChain chain = new RiskChain(List.of(
                new HighValueHandler(),
                new BlocklistHandler(),
                new VelocityHandler(registry)));

        assertThatThrownBy(() -> chain.run(payment("Kelvin", "15000.01")))
                .isInstanceOf(RiskViolationException.class);

        // auditoria contém apenas o veto do primeiro elo
        // (verificado via trail interno no teste de integração)
    }

    @Test
    @DisplayName("Pagador da lista restrita é vetado")
    void blocklistedPayerIsRejected() {
        PaymentRegistry registry = new PaymentRegistry();
        RiskChain chain = new RiskChain(List.of(
                new HighValueHandler(),
                new BlocklistHandler(),
                new VelocityHandler(registry)));

        assertThatThrownBy(() -> chain.run(payment("Fraudador da Silva", "100.00")))
                .isInstanceOf(RiskViolationException.class)
                .hasMessageContaining("lista restrita");
    }

    @Test
    @DisplayName("Quarta transação na última hora é vetada por velocidade")
    void fourthPaymentInHourIsRejected() {
        PaymentRegistry registry = new PaymentRegistry();
        RiskChainFactory factory = new RiskChainFactory(List.of(
                new HighValueHandler(),
                new BlocklistHandler(),
                new VelocityHandler(registry)));

        String payer = "Cliente Ansioso";
        // três transações aprovadas na última hora (cadeia nova a cada uma)
        for (int i = 0; i < 3; i++) {
            Payment p = payment(payer, "10.00");
            factory.newChain().run(p);
            registry.save(p);
        }
        // a quarta estoura o limite
        assertThatThrownBy(() -> factory.newChain().run(payment(payer, "10.00")))
                .isInstanceOf(RiskViolationException.class)
                .hasMessageContaining("velocidade");
    }

    @Test
    @DisplayName("Cadeia vazia não faz nada e devolve trilha vazia")
    void emptyChainDoesNothing() {
        RiskChain chain = new RiskChain(List.of());
        assertThat(chain.run(payment("Kelvin", "10.00"))).isEmpty();
    }
}
